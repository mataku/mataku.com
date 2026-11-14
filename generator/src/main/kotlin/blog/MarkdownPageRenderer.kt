package blog

object MarkdownPageRenderer {
  private const val SITE_URL = "https://mataku.com"
  private const val SITE_TITLE = "Club Kids Never Die"

  fun renderArticle(
    title: String,
    isoDate: String,
    url: String,
    tags: List<String>,
    content: String,
  ): String {
    val sb = StringBuilder()
    sb.appendLine("# $title")
    sb.appendLine()
    sb.appendLine("- Published: $isoDate")
    sb.appendLine("- URL: $url")
    if (tags.isNotEmpty()) sb.appendLine("- Tags: ${tags.joinToString(", ")}")
    sb.appendLine()
    sb.appendLine(content.trimEnd())
    return sb.toString()
  }

  internal fun renderIndex(articles: List<IndexPageGenerator.ArticleSummary>): String {
    val sb = StringBuilder()
    sb.appendLine("# $SITE_TITLE")
    sb.appendLine()
    sb.appendLine("- Feed: $SITE_URL/feed.xml")
    sb.appendLine("- Sitemap: $SITE_URL/sitemap.xml")
    sb.appendLine()
    sb.appendLine("## Articles")
    sb.appendLine()
    for (article in articles) {
      val link = "[${escapeLinkText(article.title)}]($SITE_URL/articles/${article.slug})"
      val summary = if (article.summary.isNotEmpty()) ": ${article.summary}" else ""
      sb.appendLine("- $link (${article.sortDate})$summary")
    }
    return sb.toString()
  }

  private fun escapeLinkText(value: String): String = value.replace("[", "\\[").replace("]", "\\]")
}
