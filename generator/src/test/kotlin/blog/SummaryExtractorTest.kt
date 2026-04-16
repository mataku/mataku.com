package blog

import kotlin.test.Test
import kotlin.test.assertEquals

class SummaryExtractorTest {
    @Test
    fun `strips html tags and returns plain text`() {
        val html = "<p>Hello <strong>world</strong>.</p>"
        assertEquals("Hello world.", SummaryExtractor.extract(html))
    }

    @Test
    fun `removes heading blocks entirely including their content`() {
        val html = "<h1>Title</h1><p>Body text.</p>"
        assertEquals("Body text.", SummaryExtractor.extract(html))
    }

    @Test
    fun `keeps anchor text but drops the anchor tag`() {
        val html = """<p>Go to <a href="https://example.com">the site</a> now.</p>"""
        assertEquals("Go to the site now.", SummaryExtractor.extract(html))
    }

    @Test
    fun `removes bare urls from text and collapses the resulting whitespace`() {
        val html = "<p>Visit https://example.com/path?x=1 for more.</p>"
        assertEquals("Visit for more.", SummaryExtractor.extract(html))
    }

    @Test
    fun `collapses consecutive whitespace into a single space`() {
        val html = "<p>a   b\n\nc\td</p>"
        assertEquals("a b c d", SummaryExtractor.extract(html))
    }

    @Test
    fun `truncates with ellipsis when exceeding max length`() {
        val body = "a".repeat(100)
        val result = SummaryExtractor.extract("<p>$body</p>", maxLength = 10)
        assertEquals("aaaaaaaaaa...", result)
    }

    @Test
    fun `does not append ellipsis when text fits within max length`() {
        val result = SummaryExtractor.extract("<p>short</p>", maxLength = 10)
        assertEquals("short", result)
    }
}
