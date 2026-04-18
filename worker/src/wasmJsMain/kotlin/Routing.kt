@file:OptIn(ExperimentalWasmJsInterop::class)

import kotlin.js.ExperimentalWasmJsInterop

internal sealed class Route {
  object RobotsTxt : Route()

  object SitemapXml : Route()

  object Index : Route()

  data class Asset(
    val key: String,
  ) : Route()

  data class Article(
    val slug: String,
  ) : Route()

  object NotFound : Route()
}

private fun decodeURIComponent(value: String): JsString = js("decodeURIComponent(value)")

internal fun resolveRoute(pathname: String): Route {
  if (pathname.contains("..") || pathname.contains("//")) return Route.NotFound

  val decoded =
    try {
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

internal fun contentTypeFor(filename: String): String? =
  when {
    filename.endsWith(".html") -> "text/html; charset=utf-8"
    filename.endsWith(".css") -> "text/css; charset=utf-8"
    filename.endsWith(".js") -> "application/javascript; charset=utf-8"
    filename.endsWith(".gif") -> "image/gif"
    filename.endsWith(".png") -> "image/png"
    filename.endsWith(".ico") -> "image/x-icon"
    filename.endsWith(".xml") -> "application/rss+xml; charset=utf-8"
    else -> null
  }

internal fun cacheControlFor(filename: String): String =
  when {
    filename.endsWith(".html") -> "public, max-age=86400"
    filename.endsWith(".css") || filename.endsWith(".js") -> "public, max-age=31536000, immutable"
    filename.endsWith(".gif") || filename.endsWith(".png") || filename.endsWith(".ico") -> "public, max-age=31536000, immutable"
    filename.endsWith(".json") -> "public, max-age=86400"
    filename.endsWith(".xml") -> "public, max-age=3600"
    else -> "public, max-age=3600"
  }
