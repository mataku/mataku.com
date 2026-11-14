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

private fun newAssetRequest(
  url: String,
  ifNoneMatch: String?,
): Request = js("new Request(url, ifNoneMatch ? { headers: { 'if-none-match': ifNoneMatch } } : undefined)")

private fun buildHeadersJs(
  contentType: String,
  cacheControl: String,
  vary: String? = null,
  link: String? = null,
  etag: String? = null,
): JsAny =
  js(
    """{
    const headers = {
        "content-type": contentType,
        "cache-control": cacheControl,
        "X-Content-Type-Options": "nosniff",
        "X-Frame-Options": "DENY",
        "Referrer-Policy": "strict-origin-when-cross-origin"
    };
    if (vary) headers["vary"] = vary;
    if (link) headers["link"] = link;
    if (etag) headers["etag"] = etag;
    return headers;
}""",
  )

private fun redirectHeadersJs(location: String): JsAny = js("({ location: location })")

private class RequestContext(
  val env: Env,
  val origin: String,
  val wantsMarkdown: Boolean,
  val ifNoneMatch: String?,
)

private const val HTML_CONTENT_TYPE = "text/html; charset=utf-8"
private const val MARKDOWN_CONTENT_TYPE = "text/markdown; charset=utf-8"
private const val STATUS_NOT_MODIFIED: Short = 304
private const val STATUS_TEMPORARY_REDIRECT: Short = 307

@JsExport
fun fetch(
  request: Request,
  env: JsAny,
): Promise<Response> {
  if (request.method != "GET" && request.method != "HEAD") {
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
  canonicalPathFor(pathname)?.let { canonicalPath ->
    return Promise.resolve(
      Response(null, ResponseInit(status = STATUS_TEMPORARY_REDIRECT, headers = redirectHeadersJs(canonicalPath))),
    )
  }
  val context =
    RequestContext(
      env = env.unsafeCast<Env>(),
      origin = getOrigin(url).toString(),
      wantsMarkdown = acceptsMarkdown(request.headers.get("Accept")),
      ifNoneMatch = request.headers.get("If-None-Match"),
    )
  return handleRoute(resolveRoute(pathname), context)
}

private fun notFoundHandler(context: RequestContext): Promise<Response> {
  val headers =
    buildHeadersJs(
      contentType = if (context.wantsMarkdown) MARKDOWN_CONTENT_TYPE else HTML_CONTENT_TYPE,
      cacheControl = "public, max-age=300",
      vary = "Accept",
    )
  if (context.wantsMarkdown) {
    return Promise.resolve(
      Response(NOT_FOUND_MARKDOWN.toJsString(), ResponseInit(status = 404, headers = headers)),
    )
  }
  return context.env.ASSETS
    .fetch(Request("${context.origin}/404.html".toJsString()))
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
  context: RequestContext,
): Promise<Response> =
  when (route) {
    is Route.RobotsTxt -> robotsTxtHandler()
    is Route.SitemapXml -> sitemapXmlHandler()
    is Route.NotFound -> notFoundHandler(context)
    is Route.Asset -> assetHandler(route.key, context)
    is Route.Index, is Route.Article -> pageHandler(pageFor(route)!!, context)
  }

private fun pageHandler(
  page: Page,
  context: RequestContext,
): Promise<Response> {
  val path = if (context.wantsMarkdown) page.markdownPath else page.htmlPath
  return context.env.ASSETS
    .fetch(newAssetRequest("${context.origin}/$path", context.ifNoneMatch))
    .then { response: Response ->
      val headers =
        buildHeadersJs(
          contentType = if (context.wantsMarkdown) MARKDOWN_CONTENT_TYPE else HTML_CONTENT_TYPE,
          cacheControl = PAGE_CACHE_CONTROL,
          vary = "Accept",
          link = linkHeaderFor(page),
          etag = response.headers.get("etag"),
        )
      when {
        response.ok -> Promise.resolve(Response(response.body, ResponseInit(headers = headers)))
        response.status == STATUS_NOT_MODIFIED ->
          Promise.resolve(Response(null, ResponseInit(status = STATUS_NOT_MODIFIED, headers = headers)))
        else -> notFoundHandler(context)
      }
    }.unsafeCast<Promise<Response>>()
}

private fun assetHandler(
  key: String,
  context: RequestContext,
): Promise<Response> {
  val contentType = contentTypeFor(key) ?: return notFoundHandler(context)
  return context.env.ASSETS
    .fetch(Request("${context.origin}/$key".toJsString()))
    .then { response: Response ->
      if (response.ok) {
        val headers =
          buildHeadersJs(
            contentType = contentType,
            cacheControl = cacheControlFor(key),
          )
        Promise.resolve(Response(response.body, ResponseInit(headers = headers)))
      } else {
        notFoundHandler(context)
      }
    }.unsafeCast<Promise<Response>>()
}

private fun robotsTxtHandler(): Promise<Response> {
  val headers =
    buildHeadersJs(
      contentType = "text/plain; charset=utf-8",
      cacheControl = "public, max-age=86400",
    )
  return Promise.resolve(Response(ROBOTS_TXT.toJsString(), ResponseInit(headers = headers)))
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
