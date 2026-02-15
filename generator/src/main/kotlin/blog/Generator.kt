package blog

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.getLastModifiedTime
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

        val cssFile = AssetHasher.processStylesCss(projectRoot)
        val themeJsFile = AssetHasher.processThemeJs(projectRoot)
        val articleTemplate = templatePath.readText()

        val isDev = System.getenv("DEV") == "1"
        var hasNewArticles = false

        val markdownFiles = articlesDir.listDirectoryEntries("*.md")
            .let { files ->
                if (isDev) {
                    files.filter { file ->
                        val outputFile = articlesOutputDir.resolve("${file.nameWithoutExtension}.html")
                        val isNew = !outputFile.exists()
                        if (isNew) hasNewArticles = true
                        isNew || file.getLastModifiedTime() > outputFile.getLastModifiedTime()
                    }.also { filtered ->
                        println("DEV mode: ${filtered.size}/${files.size} articles to build")
                    }
                } else {
                    files
                }
            }
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
            variables["css_file"] = cssFile
            variables["theme_js_file"] = themeJsFile
            variables["x_widgets_script"] = if (xEmbedResult.hasXEmbed) {
                """<script async src="https://platform.twitter.com/widgets.js" charset="utf-8"></script>"""
            } else ""

            val html = TemplateEngine.render(articleTemplate, variables)
            val outputFile = articlesOutputDir.resolve("$slug.html")
            outputFile.writeText(html)
            println("Generated: $outputFile")
        }

        val notFoundOutputPath = outputDir.resolve("404.html")
        if (!isDev || !notFoundOutputPath.exists() || notFoundTemplatePath.getLastModifiedTime() > notFoundOutputPath.getLastModifiedTime()) {
            generateStaticPage(notFoundTemplatePath, notFoundOutputPath, cssFile, themeJsFile)
        }

        if (!isDev || hasNewArticles) {
            IndexPageGenerator.generate()
            FeedGenerator.generate()
            SitemapGenerator.generate()
        }
    }

    private fun generateStaticPage(templatePath: Path, outputPath: Path, cssFile: String, themeJsFile: String) {
        val variables = mapOf(
            "footer" to SiteConfig.footerHtml,
            "header_actions" to SiteConfig.headerActionsHtml,
            "css_file" to cssFile,
            "theme_js_file" to themeJsFile
        )
        val html = TemplateEngine.render(templatePath.readText(), variables)
        outputPath.writeText(html)
        println("Generated: $outputPath")
    }
}
