package blog

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownPageRendererTest {
  @Test
  fun `article starts with title heading and metadata followed by the source body`() {
    val markdown =
      MarkdownPageRenderer.renderArticle(
        title = "Hello",
        isoDate = "2026-10-03",
        url = "https://mataku.com/articles/hello",
        tags = listOf("kotlin", "wasm"),
        content = "First paragraph.\n\n## Section\n\nSecond paragraph.\n",
      )

    assertEquals(
      """
      # Hello

      - Published: 2026-10-03
      - URL: https://mataku.com/articles/hello
      - Tags: kotlin, wasm

      First paragraph.

      ## Section

      Second paragraph.

      """.trimIndent(),
      markdown,
    )
  }

  @Test
  fun `article omits the tags line when there are no tags`() {
    val markdown =
      MarkdownPageRenderer.renderArticle(
        title = "Hello",
        isoDate = "2026-10-03",
        url = "https://mataku.com/articles/hello",
        tags = emptyList(),
        content = "Body.",
      )

    assertEquals(
      """
      # Hello

      - Published: 2026-10-03
      - URL: https://mataku.com/articles/hello

      Body.

      """.trimIndent(),
      markdown,
    )
  }

  @Test
  fun `index lists every article as a link with date and summary`() {
    val markdown =
      MarkdownPageRenderer.renderIndex(
        listOf(
          summary(slug = "second", title = "Second", date = "2026-10-03", summary = "About the second."),
          summary(slug = "first", title = "First", date = "2026-01-02", summary = ""),
        ),
      )

    assertEquals(
      """
      # Club Kids Never Die

      - Feed: https://mataku.com/feed.xml
      - Sitemap: https://mataku.com/sitemap.xml

      ## Articles

      - [Second](https://mataku.com/articles/second) (2026-10-03): About the second.
      - [First](https://mataku.com/articles/first) (2026-01-02)

      """.trimIndent(),
      markdown,
    )
  }

  @Test
  fun `index escapes brackets in titles so the link text stays intact`() {
    val markdown =
      MarkdownPageRenderer.renderIndex(
        listOf(summary(slug = "a", title = "[WIP] Title", date = "2026-01-02", summary = "")),
      )

    assertEquals(
      true,
      markdown.contains("""- [\[WIP\] Title](https://mataku.com/articles/a) (2026-01-02)"""),
    )
  }

  private fun summary(
    slug: String,
    title: String,
    date: String,
    summary: String,
  ) = IndexPageGenerator.ArticleSummary(
    slug = slug,
    title = title,
    displayDate = date,
    sortDate = LocalDate.parse(date),
    tags = emptyList(),
    summary = summary,
  )
}
