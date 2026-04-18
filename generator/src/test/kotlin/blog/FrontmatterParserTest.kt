package blog

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FrontmatterParserTest {
  @Test
  fun `returns raw content when no frontmatter delimiter`() {
    val raw = "# Heading\n\nBody text"
    val article = FrontmatterParser.parse(raw)
    assertEquals(emptyMap(), article.metadata)
    assertEquals(raw, article.content)
    assertTrue(article.tags.isEmpty())
  }

  @Test
  fun `returns raw content when closing delimiter is missing`() {
    val raw = "---\ntitle: Unclosed\n# Heading\n"
    val article = FrontmatterParser.parse(raw)
    assertEquals(emptyMap(), article.metadata)
    assertEquals(raw, article.content)
  }

  @Test
  fun `parses metadata and body separately`() {
    val raw = """---
title: My Post
date: 2026-01-02
---
# Heading
Body."""
    val article = FrontmatterParser.parse(raw)
    assertEquals("My Post", article.metadata["title"])
    assertEquals("2026-01-02", article.metadata["date"])
    assertEquals("# Heading\nBody.", article.content)
  }

  @Test
  fun `extracts tags as list and excludes them from metadata`() {
    val raw = """---
title: Post
tags:
  - kotlin
  - testing
---
Body."""
    val article = FrontmatterParser.parse(raw)
    assertEquals(listOf("kotlin", "testing"), article.tags)
    assertEquals(null, article.metadata["tags"])
    assertEquals("Post", article.metadata["title"])
  }

  @Test
  fun `treats non-string tag entries as excluded`() {
    val raw = """---
title: Post
tags:
  - kotlin
  - 42
---
Body."""
    val article = FrontmatterParser.parse(raw)
    assertEquals(listOf("kotlin"), article.tags)
  }

  @Test
  fun `returns empty tags when tags value is not a list`() {
    val raw = """---
title: Post
tags: single
---
Body."""
    val article = FrontmatterParser.parse(raw)
    assertTrue(article.tags.isEmpty())
  }

  @Test
  fun `trims leading whitespace before frontmatter`() {
    val raw = "\n\n---\ntitle: Trimmed\n---\nBody"
    val article = FrontmatterParser.parse(raw)
    assertEquals("Trimmed", article.metadata["title"])
    assertEquals("Body", article.content)
  }
}
