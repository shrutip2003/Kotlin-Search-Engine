package websearch

import org.jsoup.nodes.Document

data class URL(val url: String) {
    override fun toString(): String = url
}

class WebPage(val content: Document) {
    fun extractWords(): List<String> {
        val text = content.text()
        val stringWords = text.lowercase().replace(".", "").replace(",", "")
        return stringWords.split(" ")
    }
}
