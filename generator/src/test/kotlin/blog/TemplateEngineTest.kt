package blog

import kotlin.test.Test
import kotlin.test.assertEquals

class TemplateEngineTest {
  @Test
  fun `replaces a single placeholder`() {
    val result = TemplateEngine.render("Hello, {{name}}!", mapOf("name" to "World"))
    assertEquals("Hello, World!", result)
  }

  @Test
  fun `replaces multiple placeholders`() {
    val result =
      TemplateEngine.render(
        "{{greeting}}, {{name}}!",
        mapOf("greeting" to "Hi", "name" to "Kotlin"),
      )
    assertEquals("Hi, Kotlin!", result)
  }

  @Test
  fun `allows whitespace around the placeholder key`() {
    val result = TemplateEngine.render("[{{ key }}]", mapOf("key" to "value"))
    assertEquals("[value]", result)
  }

  @Test
  fun `leaves the placeholder untouched when key is missing`() {
    val result = TemplateEngine.render("{{missing}} exists", emptyMap())
    assertEquals("{{missing}} exists", result)
  }

  @Test
  fun `returns the template as-is when there are no placeholders`() {
    val template = "<p>No placeholders here.</p>"
    assertEquals(template, TemplateEngine.render(template, mapOf("x" to "y")))
  }

  @Test
  fun `does not treat keys with hyphens as placeholders`() {
    val result = TemplateEngine.render("{{my-key}}", mapOf("my-key" to "v"))
    assertEquals("{{my-key}}", result)
  }
}
