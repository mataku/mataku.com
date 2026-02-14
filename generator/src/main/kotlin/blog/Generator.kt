package blog

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.extension
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText
import kotlin.io.path.writeText

class Generator {
    private val projectRoot: Path = Path.of("").toAbsolutePath()
    private val articlesDir: Path = projectRoot.resolve("articles")
    private val outputDir: Path = projectRoot.resolve("output")
    private val articlesOutputDir: Path = outputDir.resolve("articles")
    private val templatePath: Path = projectRoot.resolve("templates/article.html")
    private val notFoundTemplatePath: Path = projectRoot.resolve("templates/404.html")

    fun run() {
        articlesOutputDir.createDirectories()

        val markdownFiles = articlesDir.listDirectoryEntries("*.md")
        if (markdownFiles.isEmpty()) {
            println("No markdown files found in $articlesDir")
            return
        }

        for (file in markdownFiles) {
            if (file.extension != "md") continue

            val raw = file.readText()
            val article = FrontmatterParser.parse(raw)

            val rawHtmlBody = MarkdownRenderer.render(article.content)
            val captionedHtml = ImageCaptionTransformer.transform(rawHtmlBody)
            val xEmbedResult = XEmbedTransformer.transform(captionedHtml)
            val gistEmbeddedHtml = GistEmbedTransformer.transform(xEmbedResult.html)
            val htmlBody = gistEmbeddedHtml

            val tagsHtml = if (article.tags.isNotEmpty()) {
                article.tags.joinToString("") { """<span class="tag">$it</span>""" }
            } else ""

            val slug = file.nameWithoutExtension

            val rawDate = article.metadata["date"] ?: ""
            val variables = article.metadata.toMutableMap()
            variables["content"] = htmlBody
            variables["tags"] = tagsHtml
            variables["date"] = DateFormatter.formatForDisplay(rawDate)
            variables["date_iso"] = DateFormatter.toIsoDate(rawDate)
            variables["url"] = "https://mataku.com/articles/$slug"
            variables["description"] = SummaryExtractor.extract(htmlBody)
            variables["footer"] = SiteConfig.footerHtml
            variables["header_actions"] = SiteConfig.headerActionsHtml
            variables["x_widgets_script"] = if (xEmbedResult.hasXEmbed) {
                """<script async src="https://platform.twitter.com/widgets.js" charset="utf-8"></script>"""
            } else ""

            val html = TemplateEngine.render(templatePath, variables)
            val outputFile = articlesOutputDir.resolve("$slug.html")
            outputFile.writeText(html)
            println("Generated: $outputFile")
        }

        generateStaticPage(notFoundTemplatePath, outputDir.resolve("404.html"))
    }

    private fun generateStaticPage(templatePath: Path, outputPath: Path) {
        val variables = mapOf(
            "footer" to SiteConfig.footerHtml,
            "header_actions" to SiteConfig.headerActionsHtml
        )
        val html = TemplateEngine.render(templatePath, variables)
        outputPath.writeText(html)
        println("Generated: $outputPath")
    }
}
