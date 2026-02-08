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
        return Promise.resolve(notFoundResponse())
    }

    val contentType = contentTypeFor(key)

    return (env.BUCKET.get(key) as Promise<dynamic>).then { obj: dynamic ->
        if (obj == null) {
            notFoundResponse()
        } else {
            val headers: dynamic = object {}
            headers["content-type"] = contentType
            Response(obj.body, ResponseInit(headers = headers))
        }
    }
}

private fun notFoundResponse(): Response {
    val headers: dynamic = object {}
    headers["content-type"] = "text/plain; charset=utf-8"
    return Response("Not Found", ResponseInit(status = 404, headers = headers))
}

private val allowedFiles = setOf("articles.js", "styles.css", "articles.json")

private fun resolveKey(pathname: String): String? {
    if (pathname.isEmpty()) return "index.html"
    if (pathname in allowedFiles) return pathname
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
        else -> "application/octet-stream"
    }
}
