package blog

import kotlin.test.Test
import kotlin.test.assertEquals

class JsonWriterTest {
    @Test
    fun `returns empty array string when no articles`() {
        val json = JsonWriter.buildArticlesJson(emptyList())
        assertEquals("[\n  \n]", json)
    }

    @Test
    fun `serializes a single article with tags`() {
        val articles = listOf(
            mapOf(
                "title" to "Hello",
                "date" to "2026-01-02",
                "path" to "/articles/hello.html",
                "tags" to listOf("kotlin", "test"),
            )
        )
        val json = JsonWriter.buildArticlesJson(articles)
        assertEquals(
            """[
  {"title":"Hello","date":"2026-01-02","path":"/articles/hello.html","tags":["kotlin","test"]}
]""",
            json
        )
    }

    @Test
    fun `serializes multiple articles joined by comma`() {
        val articles = listOf(
            mapOf(
                "title" to "A",
                "date" to "2026-01-01",
                "path" to "/a.html",
                "tags" to emptyList<String>(),
            ),
            mapOf(
                "title" to "B",
                "date" to "2026-01-02",
                "path" to "/b.html",
                "tags" to emptyList<String>(),
            ),
        )
        val json = JsonWriter.buildArticlesJson(articles)
        assertEquals(
            """[
  {"title":"A","date":"2026-01-01","path":"/a.html","tags":[]},
  {"title":"B","date":"2026-01-02","path":"/b.html","tags":[]}
]""",
            json
        )
    }

    @Test
    fun `escapes special JSON characters in title`() {
        val articles = listOf(
            mapOf(
                "title" to "Quote\"Backslash\\Newline\nTab\tCR\rBackspace\bFormfeed\u000C",
                "date" to "2026-01-01",
                "path" to "/x.html",
                "tags" to emptyList<String>(),
            )
        )
        val json = JsonWriter.buildArticlesJson(articles)
        val expectedTitle = "Quote\\\"Backslash\\\\Newline\\nTab\\tCR\\rBackspace\\bFormfeed\\f"
        assertEquals(
            """[
  {"title":"$expectedTitle","date":"2026-01-01","path":"/x.html","tags":[]}
]""",
            json
        )
    }

    @Test
    fun `escapes control characters below 0x20 as unicode`() {
        val articles = listOf(
            mapOf(
                "title" to "\u0001\u0007",
                "date" to "2026-01-01",
                "path" to "/x.html",
                "tags" to emptyList<String>(),
            )
        )
        val json = JsonWriter.buildArticlesJson(articles)
        assertEquals(
            """[
  {"title":"\u0001\u0007","date":"2026-01-01","path":"/x.html","tags":[]}
]""",
            json
        )
    }

    @Test
    fun `escapes quotes inside tags`() {
        val articles = listOf(
            mapOf(
                "title" to "T",
                "date" to "2026-01-01",
                "path" to "/x.html",
                "tags" to listOf("a\"b", "c\\d"),
            )
        )
        val json = JsonWriter.buildArticlesJson(articles)
        assertEquals(
            """[
  {"title":"T","date":"2026-01-01","path":"/x.html","tags":["a\"b","c\\d"]}
]""",
            json
        )
    }

    @Test
    fun `treats missing fields as empty strings and empty tags`() {
        val articles = listOf(mapOf<String, Any>())
        val json = JsonWriter.buildArticlesJson(articles)
        assertEquals(
            """[
  {"title":"","date":"","path":"","tags":[]}
]""",
            json
        )
    }
}
