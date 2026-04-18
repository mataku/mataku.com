package blog

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object TagLink {
  fun slug(tag: String): String = tag.lowercase().replace(Regex("[\\s/]+"), "-")

  fun urlPath(tag: String): String {
    val encoded = URLEncoder.encode(slug(tag), StandardCharsets.UTF_8).replace("+", "%20")
    return "/tags/$encoded/"
  }

  fun renderAnchors(tags: List<String>): String {
    if (tags.isEmpty()) return ""
    return tags.joinToString("") { tag ->
      """<a class="tag" href="${urlPath(tag)}">$tag</a>"""
    }
  }
}
