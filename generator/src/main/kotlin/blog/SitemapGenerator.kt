package blog

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.extension
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText
import kotlin.io.path.writeText

object SitemapGenerator {
  private val projectRoot: Path = Path.of("").toAbsolutePath()
  private val articlesDir: Path = projectRoot.resolve("articles")
  private val outputDir: Path = projectRoot.resolve("output")

  private const val SITE_URL = "https://mataku.com"

  fun generate() {
    outputDir.createDirectories()

    val markdownFiles = articlesDir.listDirectoryEntries("*.md")

    val articles =
      markdownFiles
        .filter { it.extension == "md" }
        .mapNotNull { file ->
          val raw = file.readText()
          val article = FrontmatterParser.parse(raw)
          val isDraft = article.metadata["draft"]?.toBoolean() ?: false
          if (isDraft) return@mapNotNull null

          val slug = file.nameWithoutExtension
          val date = article.metadata["date"]?.substringBefore("T")
          SitemapEntry(slug, date)
        }.sortedByDescending { it.lastmod ?: "" }

    val xml = buildSitemap(articles)
    val outputFile = outputDir.resolve("sitemap.xml")
    outputFile.writeText(xml)
    println("Generated: $outputFile")
  }

  private fun buildSitemap(articles: List<SitemapEntry>): String {
    val sb = StringBuilder()
    sb.appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
    sb.appendLine("""<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">""")

    sb.appendLine("  <url>")
    sb.appendLine("    <loc>$SITE_URL/</loc>")
    sb.appendLine("  </url>")

    sb.appendLine("  <url>")
    sb.appendLine("    <loc>$SITE_URL/feed.xml</loc>")
    sb.appendLine("  </url>")

    for (entry in articles) {
      sb.appendLine("  <url>")
      sb.appendLine("    <loc>$SITE_URL/articles/${entry.slug}</loc>")
      if (entry.lastmod != null) {
        sb.appendLine("    <lastmod>${entry.lastmod}</lastmod>")
      }
      sb.appendLine("  </url>")
    }

    sb.appendLine("</urlset>")
    return sb.toString()
  }

  private data class SitemapEntry(
    val slug: String,
    val lastmod: String?,
  )
}
