package blog

import kotlin.test.Test
import kotlin.test.assertEquals

class GistEmbedTransformerTest {
  @Test
  fun `replaces anchor to gist with embed script`() {
    val input = """<a href="https://gist.github.com/user/abc123def456">link text</a>"""
    val expected = """<script src="https://gist.github.com/user/abc123def456.js"></script>"""
    assertEquals(expected, GistEmbedTransformer.transform(input))
  }

  @Test
  fun `replaces raw gist url with embed script`() {
    val input = """<p>See https://gist.github.com/user/abc123def456 here</p>"""
    val expected = """<p>See <script src="https://gist.github.com/user/abc123def456.js"></script> here</p>"""
    assertEquals(expected, GistEmbedTransformer.transform(input))
  }

  @Test
  fun `leaves non-gist content untouched`() {
    val input = """<p>No gist links here, just https://example.com</p>"""
    assertEquals(input, GistEmbedTransformer.transform(input))
  }

  @Test
  fun `does not match gist urls that continue with a path segment`() {
    val input = """<p>https://gist.github.com/user/abc123def456/revisions</p>"""
    assertEquals(input, GistEmbedTransformer.transform(input))
  }
}
