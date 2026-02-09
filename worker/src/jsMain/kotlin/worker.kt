import org.w3c.fetch.Request
import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit
import kotlin.js.Promise

@OptIn(ExperimentalJsExport::class)
@JsExport
fun fetch(request: Request, env: dynamic): Promise<Response> {
    val url = js("new URL(request.url)")
    val pathname = (url.pathname as String).removePrefix("/")
    val key = resolveKey(pathname)

    if (key == null) {
        return notFoundResponse(env)
    }

    val contentType = contentTypeFor(key)

    return (env.BUCKET.get(key) as Promise<dynamic>).then { obj: dynamic ->
        if (obj == null) {
            notFoundResponse(env)
        } else {
            val headers: dynamic = object {}
            headers["content-type"] = contentType
            Promise.resolve(Response(obj.body, ResponseInit(headers = headers)))
        }
    }.asDynamic().unsafeCast<Promise<Response>>()
}

private fun notFoundResponse(env: dynamic): Promise<Response> {
    return (env.BUCKET.get("404.html") as Promise<dynamic>).then { obj: dynamic ->
        val headers: dynamic = object {}
        headers["content-type"] = "text/html; charset=utf-8"
        if (obj == null) {
            Response("Not Found", ResponseInit(status = 404, headers = headers))
        } else {
            Response(obj.body, ResponseInit(status = 404, headers = headers))
        }
    }
}

private fun resolveKey(pathname: String): String? {
    if (pathname.contains("..") || pathname.contains("//")) return null
    if (pathname.isEmpty()) return "index.html"
    if (pathname == "articles.json") return pathname
    if (pathname.startsWith("assets/")) return pathname
    if (pathname.startsWith("images/")) return pathname
    if (pathname.startsWith("articles/") && !pathname.contains(".")) {
        return "$pathname.html"
    }
    return null
}

private fun contentTypeFor(filename: String): String {
    return when {
        filename.endsWith(".html") -> "text/html; charset=utf-8"
        filename.endsWith(".css") -> "text/css; charset=utf-8"
        filename.endsWith(".js") -> "application/javascript; charset=utf-8"
        filename.endsWith(".json") -> "application/json; charset=utf-8"
        filename.endsWith(".gif") -> "image/gif"
        filename.endsWith(".png") -> "image/png"
        else -> "application/octet-stream"
    }
}
