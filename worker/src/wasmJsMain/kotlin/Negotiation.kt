internal data class Page(
  val htmlPath: String,
  val markdownPath: String,
)

internal const val PAGE_CACHE_CONTROL = "public, max-age=0, must-revalidate"

internal val ROBOTS_TXT =
  """
  User-agent: *
  Content-Signal: search=yes, ai-input=yes, ai-train=yes
  Allow: /
  Sitemap: https://mataku.com/sitemap.xml
  """.trimIndent()

internal val NOT_FOUND_MARKDOWN =
  """
  # 404 Not Found

  The page you requested does not exist.

  - [Home](https://mataku.com/)
  - [Sitemap](https://mataku.com/sitemap.xml)
  - [Feed](https://mataku.com/feed.xml)

  """.trimIndent()

// Wildcards such as */* do not count: browsers and generic HTTP clients send them and must keep getting HTML.
internal fun acceptsMarkdown(accept: String?): Boolean {
  if (accept.isNullOrBlank()) return false
  return accept.split(",").any { range ->
    val parts = range.split(";").map { it.trim() }
    val quality =
      parts
        .drop(1)
        .firstOrNull { it.startsWith("q=", ignoreCase = true) }
        ?.substring(2)
        ?.trim()
        ?.toDoubleOrNull() ?: 1.0
    parts.first().equals("text/markdown", ignoreCase = true) && quality > 0.0
  }
}

internal fun pageFor(route: Route): Page? =
  when (route) {
    is Route.Index -> Page(htmlPath = "", markdownPath = "index.md")
    is Route.Article -> Page(htmlPath = route.slug, markdownPath = "${route.slug}.md")
    else -> null
  }

internal fun linkHeaderFor(page: Page): String =
  listOf(
    """</sitemap.xml>; rel="sitemap"""",
    """</feed.xml>; rel="alternate"; type="application/atom+xml"""",
    """</${page.markdownPath}>; rel="alternate"; type="text/markdown"""",
  ).joinToString(", ")
