package blog

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText

object TagPageGenerator {
  private val projectRoot: Path = Path.of("").toAbsolutePath()
  private val outputDir: Path = projectRoot.resolve("output")
  private val templatePath: Path = projectRoot.resolve("templates/tag.html")

  fun generate() {
    val articles = IndexPageGenerator.collectArticles()
    if (articles.isEmpty()) {
      println("No articles found")
      return
    }

    val grouped = mutableMapOf<String, MutableList<IndexPageGenerator.ArticleSummary>>()
    for (article in articles) {
      for (tag in article.tags) {
        grouped.getOrPut(tag) { mutableListOf() }.add(article)
      }
    }
    if (grouped.isEmpty()) {
      println("No tagged articles found")
      return
    }

    val cssFile = AssetHasher.processStylesCss(projectRoot)
    val themeJsFile = AssetHasher.processThemeJs(projectRoot)
    val template = templatePath.readText()

    for ((tag, tagArticles) in grouped) {
      val articleListHtml = IndexPageGenerator.buildArticleListHtml(tagArticles)

      val variables =
        mapOf(
          "tag" to IndexPageGenerator.escapeHtml(tag),
          "article_list" to articleListHtml,
          "footer" to SiteConfig.footerHtml,
          "header_actions" to SiteConfig.headerActionsHtml,
          "css_file" to cssFile,
          "theme_js_file" to themeJsFile,
        )

      val html = TemplateEngine.render(template, variables)

      val outputPath = outputDir.resolve("tags/${TagLink.slug(tag)}/index.html")
      outputPath.parent.createDirectories()
      outputPath.writeText(html)
      println("Generated: $outputPath")
    }
  }
}
