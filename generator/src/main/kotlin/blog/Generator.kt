package blog

import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
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
            val htmlBody = HtmlGenerator(article.content, parsedTree, flavour).generateHtml()
                .removePrefix("<body>")
                .removeSuffix("</body>")

            val tagsHtml = if (article.tags.isNotEmpty()) {
                article.tags.joinToString("") { """<span class="tag">$it</span>""" }
            } else ""

            val variables = article.metadata.toMutableMap()
            variables["content"] = htmlBody
            variables["tags"] = tagsHtml

            val html = TemplateEngine.render(templatePath, variables)

            val slug = file.nameWithoutExtension
            val outputFile = articlesOutputDir.resolve("$slug.html")
            outputFile.writeText(html)
            println("Generated: $outputFile")

            articleMetadataList.add(
                mapOf(
                    "title" to (article.metadata["title"] ?: slug),
                    "date" to (article.metadata["date"] ?: ""),
                    "slug" to slug,
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
}
