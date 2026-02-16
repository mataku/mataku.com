@file:OptIn(ExperimentalJsExport::class, ExperimentalWasmJsInterop::class)

import org.w3c.fetch.Request
import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.Promise

private external interface Env : JsAny {
    val ASSETS: AssetsFetcher
}

private external interface AssetsFetcher : JsAny {
    fun fetch(request: Request): Promise<Response>
}

private sealed class Route {
    object RobotsTxt : Route()
    object SitemapXml : Route()
    object Index : Route()
    data class Asset(val key: String) : Route()
    data class Article(val slug: String) : Route()
    object NotFound : Route()
}

private fun newURL(url: String): JsAny = js("new URL(url)")

private fun getPathname(url: JsAny): JsString = js("url.pathname")

private fun getOrigin(url: JsAny): JsString = js("url.origin")

private fun decodeURIComponent(value: String): JsString = js("decodeURIComponent(value)")

private fun buildHeadersJs(
    contentType: String,
    cacheControl: String
): JsAny = js("""({
    "content-type": contentType,
    "cache-control": cacheControl,
    "X-Content-Type-Options": "nosniff",
    "X-Frame-Options": "DENY",
    "Referrer-Policy": "strict-origin-when-cross-origin"
})""")

@JsExport
fun fetch(request: Request, env: JsAny): Promise<Response> {
    val url = newURL(request.url)
    val pathname = getPathname(url).toString().removePrefix("/")
    val origin = getOrigin(url).toString()
    val route = resolveRoute(pathname)
    return handleRoute(route, env, origin)
}

private fun notFoundHandler(env: Env, origin: String): Promise<Response> {
    val headers = buildHeadersJs(
        contentType = "text/html; charset=utf-8",
        cacheControl = "public, max-age=300"
    )
    return env.ASSETS.fetch(Request("$origin/404.html".toJsString())).then { response: Response ->
        if (response.ok) {
            Response(response.body, ResponseInit(status = 404, headers = headers))
        } else {
            Response("Not Found".toJsString(), ResponseInit(status = 404, headers = headers))
        }
    }.unsafeCast<Promise<Response>>()
}

private fun handleRoute(route: Route, env: JsAny, origin: String): Promise<Response> {
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

private fun indexHandler(env: Env, origin: String): Promise<Response> {
    return fetchFromAssets("index.html", env, origin)
}

private fun assetHandler(key: String, env: Env, origin: String): Promise<Response> {
    return fetchFromAssets(key, env, origin)
}

private fun articleHandler(slug: String, env: Env, origin: String): Promise<Response> {
    return fetchFromAssets("$slug.html", env, origin)
}

private fun fetchFromAssets(key: String, env: Env, origin: String): Promise<Response> {
    val contentType = contentTypeFor(key)
    if (contentType == null) {
        return notFoundHandler(env, origin)
    }
    return env.ASSETS.fetch(Request("$origin/$key".toJsString())).then { response: Response ->
        if (response.ok) {
            val headers = buildHeadersJs(
                contentType = contentType,
                cacheControl = cacheControlFor(key)
            )
            Promise.resolve(Response(response.body, ResponseInit(headers = headers)))
        } else {
            notFoundHandler(env, origin)
        }
    }.unsafeCast<Promise<Response>>()
}

private fun resolveRoute(pathname: String): Route {
    if (pathname.contains("..") || pathname.contains("//")) return Route.NotFound

    val decoded = try {
        decodeURIComponent(pathname).toString()
    } catch (e: Throwable) {
        return Route.NotFound
    }
    if (decoded.contains("..") || decoded.contains("//")) return Route.NotFound

    // Return robots.txt and sitemap.xml directly without R2 access since crawlers request them frequently
    return when {
        pathname == "robots.txt" -> Route.RobotsTxt
        pathname == "sitemap.xml" -> Route.SitemapXml
        pathname.isEmpty() -> Route.Index
        pathname == "feed.xml" -> Route.Asset(pathname)
        pathname.startsWith("assets/") -> Route.Asset(pathname)
        pathname.startsWith("images/") -> Route.Asset(pathname)
        pathname.startsWith("articles/") && !pathname.contains(".") -> Route.Article(pathname)
        pathname.startsWith("articles/") && pathname.endsWith(".html") -> Route.Article(pathname.removeSuffix(".html"))
        else -> Route.NotFound
    }
}

private fun contentTypeFor(filename: String): String? {
    return when {
        filename.endsWith(".html") -> "text/html; charset=utf-8"
        filename.endsWith(".css") -> "text/css; charset=utf-8"
        filename.endsWith(".js") -> "application/javascript; charset=utf-8"
        filename.endsWith(".gif") -> "image/gif"
        filename.endsWith(".png") -> "image/png"
        filename.endsWith(".ico") -> "image/x-icon"
        filename.endsWith(".xml") -> "application/rss+xml; charset=utf-8"
        else -> null
    }
}

private fun robotsTxtHandler(): Promise<Response> {
    val body = """
        User-agent: *
        Allow: /
        Sitemap: https://mataku.com/sitemap.xml
    """.trimIndent()
    val headers = buildHeadersJs(
        contentType = "text/plain; charset=utf-8",
        cacheControl = "public, max-age=86400"
    )
    return Promise.resolve(Response(body.toJsString(), ResponseInit(headers = headers)))
}

private fun sitemapXmlHandler(): Promise<Response> {
    val body = """
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
    val headers = buildHeadersJs(
        contentType = "application/xml; charset=utf-8",
        cacheControl = "public, max-age=86400"
    )
    return Promise.resolve(Response(body.toJsString(), ResponseInit(headers = headers)))
}

private fun cacheControlFor(filename: String): String {
    return when {
        filename.endsWith(".html") -> "public, max-age=86400"
        filename.endsWith(".css") || filename.endsWith(".js") -> "public, max-age=31536000, immutable"
        filename.endsWith(".gif") || filename.endsWith(".png") || filename.endsWith(".ico") -> "public, max-age=31536000, immutable"
        filename.endsWith(".json") -> "public, max-age=86400"
        filename.endsWith(".xml") -> "public, max-age=3600"
        else -> "public, max-age=3600"
    }
}
