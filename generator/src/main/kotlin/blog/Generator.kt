package blog

import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import java.nio.file.Path
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
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

    fun run() {
        articlesOutputDir.createDirectories()

        val markdownFiles = articlesDir.listDirectoryEntries("*.md")
        if (markdownFiles.isEmpty()) {
            println("No markdown files found in $articlesDir")
            return
        }

        val flavour = GFMFlavourDescriptor()
        val articleMetadataList = mutableListOf<Map<String, Any>>()

        for (file in markdownFiles) {
            if (file.extension != "md") continue

            val raw = file.readText()
            val article = FrontmatterParser.parse(raw)

            val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(article.content)
            val rawHtmlBody = HtmlGenerator(article.content, parsedTree, flavour).generateHtml()
                .removePrefix("<body>")
                .removeSuffix("</body>")
            val captionedHtml = ImageCaptionTransformer.transform(rawHtmlBody)
            val xEmbedResult = XEmbedTransformer.transform(captionedHtml)
            val htmlBody = xEmbedResult.html

            val tagsHtml = if (article.tags.isNotEmpty()) {
                article.tags.joinToString("") { """<span class="tag">$it</span>""" }
            } else ""

            val slug = file.nameWithoutExtension

            val variables = article.metadata.toMutableMap()
            variables["content"] = htmlBody
            variables["tags"] = tagsHtml
            variables["date"] = formatDateForDisplay(article.metadata["date"] ?: "")
            variables["url"] = "https://mataku.com/articles/$slug"
            variables["description"] = generateDescription(htmlBody)
            variables["x_widgets_script"] = if (xEmbedResult.hasXEmbed) {
                """<script async src="https://platform.twitter.com/widgets.js" charset="utf-8"></script>"""
            } else ""

            val html = TemplateEngine.render(templatePath, variables)
            val outputFile = articlesOutputDir.resolve("$slug.html")
            outputFile.writeText(html)
            println("Generated: $outputFile")

            articleMetadataList.add(
                mapOf(
                    "title" to (article.metadata["title"] ?: slug),
                    "date" to formatDateForDisplay(article.metadata["date"] ?: ""),
                    "path" to "/articles/$slug",
                    "tags" to article.tags
                )
            )
        }

        val sortedArticles = articleMetadataList.sortedByDescending { (it["date"] as? String) ?: "" }
        val json = JsonWriter.buildArticlesJson(sortedArticles)
        val jsonOutputFile = outputDir.resolve("articles.json")
        jsonOutputFile.writeText(json)
        println("Generated: $jsonOutputFile")
    }

    private fun formatDateForDisplay(dateString: String): String {
        if (dateString.isBlank()) return ""

        return try {
            val offsetDateTime = OffsetDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            offsetDateTime.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: DateTimeParseException) {
            try {
                LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
                dateString
            } catch (e: DateTimeParseException) {
                throw IllegalArgumentException("Invalid date format: $dateString")
            }
        }
    }

    private fun generateDescription(htmlBody: String, maxLength: Int = 80): String {
        val withoutHeaders = htmlBody.replace(Regex("<h[1-6][^>]*>.*?</h[1-6]>", RegexOption.DOT_MATCHES_ALL), "")
        val withoutLinks = withoutHeaders.replace(Regex("<a[^>]*>(.*?)</a>")) { it.groupValues[1] }
        val text = withoutLinks.replace(Regex("<[^>]+>"), "")
            .replace(Regex("https?://[a-zA-Z0-9./?=&#_%-]+"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
        return if (text.length > maxLength) {
            text.take(maxLength) + "..."
        } else {
            text
        }
    }
}
