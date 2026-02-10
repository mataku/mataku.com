import org.w3c.fetch.Request
import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit
import kotlin.js.Promise

@OptIn(ExperimentalJsExport::class)
@JsExport
fun fetch(request: Request, env: dynamic): Promise<Response> {
    val url = js("new URL(request.url)")
    val pathname = (url.pathname as String).removePrefix("/")

    // Return robots.txt and sitemap.xml directly without R2 access since crawlers request them frequently
    if (pathname == "robots.txt") {
        return robotsTxtResponse()
    }
    if (pathname == "sitemap.xml") {
        return sitemapXmlResponse()
    }

    val key = resolveKey(pathname)

    if (key == null) {
        return notFoundResponse(env)
    }

    val contentType = contentTypeFor(key)
    if (contentType == null) {
        return notFoundResponse(env)
    }

    return (env.BUCKET.get(key) as Promise<dynamic>).then { obj: dynamic ->
        if (obj == null) {
            notFoundResponse(env)
        } else {
            val headers: dynamic = object {}
            headers["content-type"] = contentType
            headers["cache-control"] = cacheControlFor(key)
            setSecurityHeaders(headers)
            Promise.resolve(Response(obj.body, ResponseInit(headers = headers)))
        }
    }.asDynamic().unsafeCast<Promise<Response>>()
}

private fun notFoundResponse(env: dynamic): Promise<Response> {
    return (env.BUCKET.get("404.html") as Promise<dynamic>).then { obj: dynamic ->
        val headers: dynamic = object {}
        headers["content-type"] = "text/html; charset=utf-8"
        headers["cache-control"] = "public, max-age=300"
        setSecurityHeaders(headers)
        if (obj == null) {
            Response("Not Found", ResponseInit(status = 404, headers = headers))
        } else {
            Response(obj.body, ResponseInit(status = 404, headers = headers))
        }
    }
}

private fun resolveKey(pathname: String): String? {
    if (pathname.contains("..") || pathname.contains("//")) return null

    val decoded = try {
        js("decodeURIComponent(pathname)") as String
    } catch (e: Throwable) {
        return null
    }
    if (decoded.contains("..") || decoded.contains("//")) return null

    if (pathname.isEmpty()) return "index.html"
    if (pathname == "articles.json") return pathname
    if (pathname == "feed.xml") return pathname
    if (pathname.startsWith("assets/")) return pathname
    if (pathname.startsWith("images/")) return pathname
    if (pathname.startsWith("articles/") && !pathname.contains(".")) {
        return "$pathname.html"
    }
    return null
}

private fun contentTypeFor(filename: String): String? {
    return when {
        filename.endsWith(".html") -> "text/html; charset=utf-8"
        filename.endsWith(".css") -> "text/css; charset=utf-8"
        filename.endsWith(".js") -> "application/javascript; charset=utf-8"
        filename.endsWith(".json") -> "application/json; charset=utf-8"
        filename.endsWith(".gif") -> "image/gif"
        filename.endsWith(".png") -> "image/png"
        filename.endsWith(".ico") -> "image/x-icon"
        filename.endsWith(".xml") -> "application/rss+xml; charset=utf-8"
        else -> null
    }
}

private fun robotsTxtResponse(): Promise<Response> {
    val body = """
        User-agent: *
        Allow: /
        Sitemap: https://mataku.com/sitemap.xml
    """.trimIndent()
    val headers: dynamic = object {}
    headers["content-type"] = "text/plain; charset=utf-8"
    headers["cache-control"] = "public, max-age=86400"
    setSecurityHeaders(headers)
    return Promise.resolve(Response(body, ResponseInit(headers = headers)))
}

private fun sitemapXmlResponse(): Promise<Response> {
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
    val headers: dynamic = object {}
    headers["content-type"] = "application/xml; charset=utf-8"
    headers["cache-control"] = "public, max-age=86400"
    setSecurityHeaders(headers)
    return Promise.resolve(Response(body, ResponseInit(headers = headers)))
}

private fun setSecurityHeaders(headers: dynamic) {
    headers["X-Content-Type-Options"] = "nosniff"
    headers["X-Frame-Options"] = "DENY"
    headers["Referrer-Policy"] = "strict-origin-when-cross-origin"
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
