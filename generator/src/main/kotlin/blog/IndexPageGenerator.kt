package blog

import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.io.path.createDirectories
import kotlin.io.path.extension
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText
import kotlin.io.path.writeText

object IndexPageGenerator {
    private const val ARTICLES_PER_PAGE = 20

    private val projectRoot: Path = Path.of("").toAbsolutePath()
    private val articlesDir: Path = projectRoot.resolve("articles")
    private val outputDir: Path = projectRoot.resolve("output")
    private val templatePath: Path = projectRoot.resolve("templates/index.html")

    private val footerHtml = """
        <footer>
            &copy; Takuma Homma
            <span class="footer-credit">Made with <a href="https://kotlinlang.org" target="_blank" rel="noopener">Kotlin</a></span>
        </footer>
    """.trimIndent()

    fun generate() {
        val articles = collectArticles()
        if (articles.isEmpty()) {
            println("No articles found")
            return
        }

        val totalPages = (articles.size + ARTICLES_PER_PAGE - 1) / ARTICLES_PER_PAGE

        for (pageNum in 1..totalPages) {
            val startIndex = (pageNum - 1) * ARTICLES_PER_PAGE
            val endIndex = minOf(startIndex + ARTICLES_PER_PAGE, articles.size)
            val pageArticles = articles.subList(startIndex, endIndex)

            val articleListHtml = buildArticleListHtml(pageArticles)
            val paginationHtml = buildPaginationHtml(pageNum, totalPages)

            val variables = mapOf(
                "article_list" to articleListHtml,
                "pagination" to paginationHtml,
                "footer" to footerHtml
            )

            val html = TemplateEngine.render(templatePath, variables)

            val outputPath = if (pageNum == 1) {
                outputDir.resolve("index.html")
            } else {
                outputDir.resolve("page/$pageNum/index.html")
            }
            outputPath.parent.createDirectories()
            outputPath.writeText(html)
            println("Generated: $outputPath")
        }
    }

    private fun collectArticles(): List<ArticleSummary> {
        val markdownFiles = articlesDir.listDirectoryEntries("*.md")

        val extensions = listOf(
            TablesExtension.create(),
            StrikethroughExtension.create(),
            AutolinkExtension.create()
        )
        val parser = Parser.builder().extensions(extensions).build()
        val renderer = HtmlRenderer.builder().extensions(extensions).build()

        return markdownFiles
            .filter { it.extension == "md" }
            .mapNotNull { file ->
                val raw = file.readText()
                val article = FrontmatterParser.parse(raw)
                val dateStr = article.metadata["date"] ?: return@mapNotNull null
                val title = article.metadata["title"] ?: return@mapNotNull null
                val isDraft = article.metadata["draft"]?.toBoolean() ?: false
                if (isDraft) return@mapNotNull null

                val slug = file.nameWithoutExtension

                val document = parser.parse(article.content)
                val htmlBody = renderer.render(document)
                val summary = generateSummary(htmlBody)

                val sortDate = LocalDate.parse(dateStr.substringBefore("T"))
                ArticleSummary(
                    slug = slug,
                    title = title,
                    displayDate = formatDateForDisplay(dateStr),
                    sortDate = sortDate,
                    tags = article.tags,
                    summary = summary
                )
            }
            .sortedByDescending { it.sortDate }
    }

    private fun buildArticleListHtml(articles: List<ArticleSummary>): String {
        return articles.joinToString("\n") { article ->
            val tagsHtml = if (article.tags.isNotEmpty()) {
                val tagSpans = article.tags.joinToString("") {
                    """<span class="tag">${escapeHtml(it)}</span>"""
                }
                """<div class="tags">$tagSpans</div>"""
            } else ""

            val summaryHtml = if (article.summary.isNotEmpty()) {
                """<p class="summary">${escapeHtml(article.summary)}</p>"""
            } else ""

            """
                <a href="/articles/${article.slug}" class="article-card">
                    <h2>${escapeHtml(article.title)}</h2>
                    <time>${article.displayDate}</time>
                    $summaryHtml
                    $tagsHtml
                </a>
            """.trimIndent()
        }
    }

    private fun buildPaginationHtml(currentPage: Int, totalPages: Int): String {
        if (totalPages <= 1) return ""

        val sb = StringBuilder()
        sb.append("""<nav class="pagination">""")

        if (currentPage > 1) {
            val prevUrl = if (currentPage == 2) "/" else "/page/${currentPage - 1}/"
            sb.append("""<a href="$prevUrl" class="pagination-prev">Prev</a>""")
        } else {
            sb.append("""<span class="pagination-prev disabled">Prev</span>""")
        }

        sb.append("""<span class="pagination-info">$currentPage / $totalPages</span>""")

        if (currentPage < totalPages) {
            sb.append("""<a href="/page/${currentPage + 1}/" class="pagination-next">Next</a>""")
        } else {
            sb.append("""<span class="pagination-next disabled">Next</span>""")
        }

        sb.append("</nav>")
        return sb.toString()
    }

    private fun formatDateForDisplay(dateString: String): String {
        if (dateString.isBlank()) return ""
        val dateOnly = dateString.substringBefore("T")
        return LocalDate.parse(dateOnly).format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    private fun generateSummary(htmlBody: String, maxLength: Int = 80): String {
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

    private fun escapeHtml(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
    }

    private data class ArticleSummary(
        val slug: String,
        val title: String,
        val displayDate: String,
        val sortDate: LocalDate,
        val tags: List<String>,
        val summary: String
    )
}
