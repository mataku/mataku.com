package blog

import kotlin.test.Test
import kotlin.test.assertEquals

class ImageCaptionTransformerTest {
  @Test
  fun `wraps img with title in figure and figcaption`() {
    val input = """<img src="/a.png" alt="alt text" title="My caption" />"""
    val expected = """<figure><img src="/a.png" alt="alt text"><figcaption>My caption</figcaption></figure>"""
    assertEquals(expected, ImageCaptionTransformer.transform(input))
  }

  @Test
  fun `supports img without self-closing slash`() {
    val input = """<img src="/a.png" alt="alt" title="Cap">"""
    val expected = """<figure><img src="/a.png" alt="alt"><figcaption>Cap</figcaption></figure>"""
    assertEquals(expected, ImageCaptionTransformer.transform(input))
  }

  @Test
  fun `leaves img without title attribute untouched`() {
    val input = """<img src="/a.png" alt="alt" />"""
    assertEquals(input, ImageCaptionTransformer.transform(input))
  }

  @Test
  fun `transforms multiple images in the same html`() {
    val input = """<p><img src="/1.png" alt="one" title="First" /></p>
<p><img src="/2.png" alt="two" title="Second" /></p>"""
    val expected = """<p><figure><img src="/1.png" alt="one"><figcaption>First</figcaption></figure></p>
<p><figure><img src="/2.png" alt="two"><figcaption>Second</figcaption></figure></p>"""
    assertEquals(expected, ImageCaptionTransformer.transform(input))
  }
}
