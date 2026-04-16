package blog

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class XEmbedTransformerTest {
    @Test
    fun `converts anchor tag pointing to x dot com status to twitter blockquote`() {
        val input = """<p><a href="https://x.com/jack/status/20">link</a></p>"""
        val result = XEmbedTransformer.transform(input)
        assertEquals(
            """<p><blockquote class="twitter-tweet"><a href="https://twitter.com/jack/status/20"></a></blockquote></p>""",
            result.html,
        )
        assertTrue(result.hasXEmbed)
    }

    @Test
    fun `converts raw x dot com url to twitter blockquote`() {
        val input = """<p>See https://x.com/jack/status/42 now.</p>"""
        val result = XEmbedTransformer.transform(input)
        assertEquals(
            """<p>See <blockquote class="twitter-tweet"><a href="https://twitter.com/jack/status/42"></a></blockquote> now.</p>""",
            result.html,
        )
        assertTrue(result.hasXEmbed)
    }

    @Test
    fun `returns hasXEmbed false when no x url is present`() {
        val input = """<p>Just some text with https://example.com.</p>"""
        val result = XEmbedTransformer.transform(input)
        assertEquals(input, result.html)
        assertFalse(result.hasXEmbed)
    }

    @Test
    fun `does not double-embed a url already inside an anchor that was replaced`() {
        val input = """<a href="https://x.com/jack/status/1">click</a>"""
        val result = XEmbedTransformer.transform(input)
        assertEquals(
            """<blockquote class="twitter-tweet"><a href="https://twitter.com/jack/status/1"></a></blockquote>""",
            result.html,
        )
    }
}
