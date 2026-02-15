package blog

object TemplateEngine {
    private val PLACEHOLDER_REGEX = Regex("\\{\\{\\s*(\\w+)\\s*}}")

    fun render(template: String, variables: Map<String, String>): String {
        return PLACEHOLDER_REGEX.replace(template) { match ->
            val key = match.groupValues[1]
            variables[key] ?: match.value
        }
    }
}
