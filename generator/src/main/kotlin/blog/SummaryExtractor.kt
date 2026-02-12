package blog

object SummaryExtractor {
    fun extract(html: String, maxLength: Int = 80): String {
        val withoutHeaders = html.replace(Regex("<h[1-6][^>]*>.*?</h[1-6]>", RegexOption.DOT_MATCHES_ALL), "")
        val withoutLinks = withoutHeaders.replace(Regex("<a[^>]*>(.*?)</a>")) { it.groupValues[1] }
        val text = withoutLinks.replace(Regex("<[^>]+>"), "")
            .replace(Regex("https?://[a-zA-Z0-9./?=&#_%-]+"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
        return if (text.length > maxLength) {
            text.take(maxLength) + "..."
        } else {
            text
        }
    }
}
