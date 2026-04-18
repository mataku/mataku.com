@file:OptIn(ExperimentalJsExport::class, ExperimentalWasmJsInterop::class)

import org.w3c.fetch.Request
import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.Promise

private external interface Env : JsAny {
  @Suppress("PropertyName")
  val ASSETS: AssetsFetcher
}

private external interface AssetsFetcher : JsAny {
  fun fetch(request: Request): Promise<Response>
}

private fun newURL(url: String): JsAny = js("new URL(url)")

private fun getPathname(url: JsAny): JsString = js("url.pathname")

private fun getOrigin(url: JsAny): JsString = js("url.origin")

private fun buildHeadersJs(
  contentType: String,
  cacheControl: String,
): JsAny =
  js(
    """({
    "content-type": contentType,
    "cache-control": cacheControl,
    "X-Content-Type-Options": "nosniff",
    "X-Frame-Options": "DENY",
    "Referrer-Policy": "strict-origin-when-cross-origin"
})""",
  )

@JsExport
fun fetch(
  request: Request,
  env: JsAny,
): Promise<Response> {
  if (request.method != "GET") {
    val headers =
      buildHeadersJs(
        contentType = "text/plain; charset=utf-8",
        cacheControl = "no-store",
      )
    return Promise.resolve(
      Response("Not Found".toJsString(), ResponseInit(status = 404, headers = headers)),
    )
  }
  val url = newURL(request.url)
  val pathname = getPathname(url).toString().removePrefix("/")
  val origin = getOrigin(url).toString()
  val route = resolveRoute(pathname)
  return handleRoute(route, env, origin)
}

private fun notFoundHandler(
  env: Env,
  origin: String,
): Promise<Response> {
  val headers =
    buildHeadersJs(
      contentType = "text/html; charset=utf-8",
      cacheControl = "public, max-age=300",
    )
  return env.ASSETS
    .fetch(Request("$origin/404.html".toJsString()))
    .then { response: Response ->
      if (response.ok) {
        Response(response.body, ResponseInit(status = 404, headers = headers))
      } else {
        Response("Not Found".toJsString(), ResponseInit(status = 404, headers = headers))
      }
    }.unsafeCast<Promise<Response>>()
}

private fun handleRoute(
  route: Route,
  env: JsAny,
  origin: String,
): Promise<Response> {
  val typedEnv = env.unsafeCast<Env>()
  return when (route) {
    is Route.RobotsTxt -> robotsTxtHandler()
    is Route.SitemapXml -> sitemapXmlHandler()
    is Route.NotFound -> notFoundHandler(typedEnv, origin)
    is Route.Index -> indexHandler(typedEnv, origin)
    is Route.Asset -> assetHandler(route.key, typedEnv, origin)
    is Route.Article -> articleHandler(route.slug, typedEnv, origin)
  }
}

private fun indexHandler(
  env: Env,
  origin: String,
): Promise<Response> = fetchFromAssets("index.html", env, origin)

private fun assetHandler(
  key: String,
  env: Env,
  origin: String,
): Promise<Response> = fetchFromAssets(key, env, origin)

private fun articleHandler(
  slug: String,
  env: Env,
  origin: String,
): Promise<Response> = fetchFromAssets("$slug.html", env, origin)

private fun fetchFromAssets(
  key: String,
  env: Env,
  origin: String,
): Promise<Response> {
  val contentType = contentTypeFor(key)
  if (contentType == null) {
    return notFoundHandler(env, origin)
  }
  return env.ASSETS
    .fetch(Request("$origin/$key".toJsString()))
    .then { response: Response ->
      if (response.ok) {
        val headers =
          buildHeadersJs(
            contentType = contentType,
            cacheControl = cacheControlFor(key),
          )
        Promise.resolve(Response(response.body, ResponseInit(headers = headers)))
      } else {
        notFoundHandler(env, origin)
      }
    }.unsafeCast<Promise<Response>>()
}

private fun robotsTxtHandler(): Promise<Response> {
  val body =
    """
    User-agent: *
    Allow: /
    Sitemap: https://mataku.com/sitemap.xml
    """.trimIndent()
  val headers =
    buildHeadersJs(
      contentType = "text/plain; charset=utf-8",
      cacheControl = "public, max-age=86400",
    )
  return Promise.resolve(Response(body.toJsString(), ResponseInit(headers = headers)))
}

private fun sitemapXmlHandler(): Promise<Response> {
  val body =
    """
    <?xml version="1.0" encoding="UTF-8"?>
    <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
      <url>
        <loc>https://mataku.com/</loc>
      </url>
      <url>
        <loc>https://mataku.com/feed.xml</loc>
      </url>
    </urlset>
    """.trimIndent()
  val headers =
    buildHeadersJs(
      contentType = "application/xml; charset=utf-8",
      cacheControl = "public, max-age=86400",
    )
  return Promise.resolve(Response(body.toJsString(), ResponseInit(headers = headers)))
}
