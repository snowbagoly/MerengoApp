package hu.bagoly.snow.merengoapp.hu.bagoly.snow.merengoapp.query

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class StoryPageParser(val id: String) {
    val storyUrl = "https://fanfic.hu/merengo/viewstory.php?sid=$id&i=1" //TODO extract this to input variable for testing; or the doc
    val userIdRegex = Regex("uid=(\\d+)")
    val doc : Document
    val title : String
    val author : String
    val authorId : String?
    val currentChapterTitle : String
    val content : String

    init {
        doc = Jsoup.connect(storyUrl).get()

        val headerElement = doc.select("table table tr td p")
        val titleElement = headerElement.select("a").get(0)
        title = titleElement.text()

        val authorElement = headerElement.select("a").get(1)
        author = authorElement.text()
        authorId = userIdRegex.find(authorElement.attr("href"))?.groups?.get(1)?.value
        println("title: $title - id: $id - author: $author ($authorId)")

        val chapterSelectorElement = doc.select("table table form select")
        val currentChapterElement = chapterSelectorElement.select("option[selected]")
        currentChapterTitle = currentChapterElement.text()
        println("$currentChapterTitle")

        val contentNodes = chapterSelectorElement.parents().get(1).siblingNodes().dropLast(2)
        content = contentNodes.joinToString("\n")
        println("$content")
    }
}

fun main() { //TODO move this to a test
    StoryPageParser("211")
}