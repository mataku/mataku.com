import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NegotiationTest {
  @Test
  fun acceptsMarkdown_markdownOnly_returnsTrue() {
    assertTrue(acceptsMarkdown("text/markdown"))
  }

  @Test
  fun acceptsMarkdown_markdownAmongOtherTypes_returnsTrue() {
    assertTrue(acceptsMarkdown("text/markdown, text/html;q=0.9, */*;q=0.1"))
  }

  @Test
  fun acceptsMarkdown_caseAndWhitespaceVariations_returnsTrue() {
    assertTrue(acceptsMarkdown("text/html , Text/Markdown ; q=0.8"))
  }

  @Test
  fun acceptsMarkdown_browserAcceptHeader_returnsFalse() {
    assertFalse(acceptsMarkdown("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"))
  }

  @Test
  fun acceptsMarkdown_wildcardOnly_returnsFalse() {
    assertFalse(acceptsMarkdown("*/*"))
    assertFalse(acceptsMarkdown("text/*"))
  }

  @Test
  fun acceptsMarkdown_markdownWithZeroQuality_returnsFalse() {
    assertFalse(acceptsMarkdown("text/markdown;q=0, text/html"))
    assertFalse(acceptsMarkdown("text/markdown; q=0.0"))
  }

  @Test
  fun acceptsMarkdown_missingHeader_returnsFalse() {
    assertFalse(acceptsMarkdown(null))
    assertFalse(acceptsMarkdown(""))
  }

  @Test
  fun pageFor_index_returnsHtmlAndMarkdownAssetPaths() {
    assertEquals(Page(htmlPath = "", markdownPath = "index.md"), pageFor(Route.Index))
  }

  @Test
  fun pageFor_article_returnsHtmlAndMarkdownAssetPaths() {
    assertEquals(
      Page(htmlPath = "articles/my-article", markdownPath = "articles/my-article.md"),
      pageFor(Route.Article("articles/my-article")),
    )
  }

  @Test
  fun pageFor_nonPageRoutes_returnsNull() {
    assertEquals(null, pageFor(Route.Asset("feed.xml")))
    assertEquals(null, pageFor(Route.NotFound))
    assertEquals(null, pageFor(Route.RobotsTxt))
  }

  @Test
  fun linkHeaderFor_page_advertisesSitemapFeedAndMarkdownAlternate() {
    assertEquals(
      """</sitemap.xml>; rel="sitemap", """ +
        """</feed.xml>; rel="alternate"; type="application/atom+xml", """ +
        """</articles/my-article.md>; rel="alternate"; type="text/markdown"""",
      linkHeaderFor(Page(htmlPath = "articles/my-article", markdownPath = "articles/my-article.md")),
    )
  }

  @Test
  fun robotsTxt_declaresContentSignalsAndSitemap() {
    assertEquals(
      """
      User-agent: *
      Content-Signal: search=yes, ai-input=yes, ai-train=yes
      Allow: /
      Sitemap: https://mataku.com/sitemap.xml
      """.trimIndent(),
      ROBOTS_TXT,
    )
  }

  @Test
  fun notFoundMarkdown_explainsAndLinksToEntryPoints() {
    assertContains(NOT_FOUND_MARKDOWN, "# 404 Not Found")
    assertContains(NOT_FOUND_MARKDOWN, "(https://mataku.com/)")
    assertContains(NOT_FOUND_MARKDOWN, "(https://mataku.com/sitemap.xml)")
    assertContains(NOT_FOUND_MARKDOWN, "(https://mataku.com/feed.xml)")
  }
}
