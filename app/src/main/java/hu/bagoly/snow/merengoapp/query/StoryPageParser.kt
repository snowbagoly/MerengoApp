package hu.bagoly.snow.merengoapp.query

import org.jsoup.nodes.Document

class StoryPageParser {
    val userIdRegex = Regex("uid=(\\d+)")
    var title: String? = null
    var author: String? = null
    var authorId: String? = null
    var currentChapterTitle: String? = null
    var content: String? = null

    fun parse(doc: Document) {

        val headerElement = doc.select("table table tr td p")
        val titleElement = headerElement.select("a").get(0)
        title = titleElement.text()

        val authorElement = headerElement.select("a").get(1)
        author = authorElement.text()
        authorId = userIdRegex.find(authorElement.attr("href"))?.groups?.get(1)?.value

        val chapterSelectorElement = doc.select("table table form select")
        val currentChapterElement = chapterSelectorElement.select("option[selected]")
        currentChapterTitle = currentChapterElement.first().text()

        val contentNodes = chapterSelectorElement.parents().get(1).siblingNodes()
        content = contentNodes.joinToString("").replace("\\s+".toRegex(), " ")

        // Eliminating the chapter selectors and other form elements
        for (formElement in doc.select("table table form"))
        {
            content = content?.replace(formElement.toString().replace("\\s+".toRegex(), " "), "")
        }
    }
}