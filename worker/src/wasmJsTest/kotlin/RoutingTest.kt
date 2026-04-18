import kotlin.test.Test
import kotlin.test.assertEquals

class RoutingTest {
  @Test
  fun resolveRoute_emptyPathname_returnsIndex() {
    assertEquals(Route.Index, resolveRoute(""))
  }

  @Test
  fun resolveRoute_robotsTxt_returnsRobotsTxt() {
    assertEquals(Route.RobotsTxt, resolveRoute("robots.txt"))
  }

  @Test
  fun resolveRoute_sitemapXml_returnsSitemapXml() {
    assertEquals(Route.SitemapXml, resolveRoute("sitemap.xml"))
  }

  @Test
  fun resolveRoute_feedXml_returnsAsset() {
    assertEquals(Route.Asset("feed.xml"), resolveRoute("feed.xml"))
  }

  @Test
  fun resolveRoute_assetsPath_returnsAsset() {
    assertEquals(Route.Asset("assets/styles.abc123.css"), resolveRoute("assets/styles.abc123.css"))
  }

  @Test
  fun resolveRoute_imagesPath_returnsAsset() {
    assertEquals(Route.Asset("images/foo.png"), resolveRoute("images/foo.png"))
  }

  @Test
  fun resolveRoute_articleWithoutExtension_returnsArticle() {
    assertEquals(Route.Article("articles/my-article"), resolveRoute("articles/my-article"))
  }

  @Test
  fun resolveRoute_articleWithHtmlExtension_stripsHtml() {
    assertEquals(Route.Article("articles/my-article"), resolveRoute("articles/my-article.html"))
  }

  @Test
  fun resolveRoute_articleWithOtherExtension_returnsNotFound() {
    assertEquals(Route.NotFound, resolveRoute("articles/my-article.json"))
  }

  @Test
  fun resolveRoute_pathWithDotDot_returnsNotFound() {
    assertEquals(Route.NotFound, resolveRoute("../etc/passwd"))
  }

  @Test
  fun resolveRoute_pathWithDoubleSlash_returnsNotFound() {
    assertEquals(Route.NotFound, resolveRoute("foo//bar"))
  }

  @Test
  fun resolveRoute_encodedDotDot_returnsNotFound() {
    assertEquals(Route.NotFound, resolveRoute("articles/%2e%2e/secret"))
  }

  @Test
  fun resolveRoute_unknownPath_returnsNotFound() {
    assertEquals(Route.NotFound, resolveRoute("unknown/path"))
  }

  @Test
  fun contentTypeFor_knownExtensions_returnsMimeType() {
    assertEquals("text/html; charset=utf-8", contentTypeFor("index.html"))
    assertEquals("text/css; charset=utf-8", contentTypeFor("styles.css"))
    assertEquals("application/javascript; charset=utf-8", contentTypeFor("theme.js"))
    assertEquals("image/gif", contentTypeFor("foo.gif"))
    assertEquals("image/png", contentTypeFor("foo.png"))
    assertEquals("image/x-icon", contentTypeFor("favicon.ico"))
    assertEquals("application/rss+xml; charset=utf-8", contentTypeFor("feed.xml"))
  }

  @Test
  fun contentTypeFor_unknownExtension_returnsNull() {
    assertEquals(null, contentTypeFor("foo.bin"))
    assertEquals(null, contentTypeFor("noext"))
  }

  @Test
  fun cacheControlFor_html_returnsOneDay() {
    assertEquals("public, max-age=86400", cacheControlFor("index.html"))
  }

  @Test
  fun cacheControlFor_cssAndJs_returnsImmutable() {
    assertEquals("public, max-age=31536000, immutable", cacheControlFor("styles.css"))
    assertEquals("public, max-age=31536000, immutable", cacheControlFor("theme.js"))
  }

  @Test
  fun cacheControlFor_images_returnsImmutable() {
    assertEquals("public, max-age=31536000, immutable", cacheControlFor("foo.gif"))
    assertEquals("public, max-age=31536000, immutable", cacheControlFor("foo.png"))
    assertEquals("public, max-age=31536000, immutable", cacheControlFor("favicon.ico"))
  }

  @Test
  fun cacheControlFor_xml_returnsOneHour() {
    assertEquals("public, max-age=3600", cacheControlFor("feed.xml"))
  }

  @Test
  fun cacheControlFor_json_returnsOneDay() {
    assertEquals("public, max-age=86400", cacheControlFor("articles.json"))
  }

  @Test
  fun cacheControlFor_unknown_returnsOneHour() {
    assertEquals("public, max-age=3600", cacheControlFor("noext"))
  }
}
