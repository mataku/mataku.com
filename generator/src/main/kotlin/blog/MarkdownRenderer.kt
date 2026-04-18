package blog

import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

object MarkdownRenderer {
  private val extensions =
    listOf(
      TablesExtension.create(),
      StrikethroughExtension.create(),
      AutolinkExtension.create(),
    )
  private val parser = Parser.builder().extensions(extensions).build()
  private val renderer = HtmlRenderer.builder().extensions(extensions).build()

  fun render(markdown: String): String {
    val document = parser.parse(markdown)
    return renderer.render(document)
  }
}
