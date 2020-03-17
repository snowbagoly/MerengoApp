package hu.bagoly.snow.merengoapp.hu.bagoly.snow.merengoapp.query

import hu.bagoly.snow.merengoapp.hu.bagoly.snow.merengoapp.model.StoryDescriptor
import org.jsoup.nodes.Document

class RecentPageParser() {
    val idRegex = Regex("sid=(\\d+)")
    val descriptors = ArrayList<StoryDescriptor>()
    fun parse(doc: Document) {
        val storyDescriptors = doc.select(".mainnav")
        for (story in storyDescriptors) {
            val titleElement = story.select("span.storytitle a")
            val isAdultContent = titleElement.attr("href").contains("javascript")
            val title = titleElement.text()
            val id = idRegex.find(titleElement.attr("href"))?.groups?.get(1)?.value

            val author = "TODO"
            val criticsCount = "TODO"

            val description = story.select("tr").get(1).select("td").text()

            val category = story.select("tr").get(3).select("td").get(1).text()
            val cast = story.select("tr").get(4).select("td").get(1).text()

            val ageLimit = story.select("tr").get(5).select("td").get(1).text()
            val warnings = story.select("tr").get(5).select("td").get(3).text()

            val properties = story.select("tr").get(6).select("td").get(1).text()
            val chapterCount = story.select("tr").get(6).select("td").get(3).text()

            val creationDate = story.select("tr").get(7).select("td").get(1).text()
            val lastRefreshDate = story.select("tr").get(7).select("td").get(3).text()

            val wordCount = story.select("tr").get(8).select("td").get(1).text()
            val isFinished = "Igen" == story.select("tr").get(8).select("td").get(3).text()

            println("title: $title ($isAdultContent) - id: $id\r\nDescription: $description")
            println("Category: $category - Cast: $cast")
            println("Agelimit: $ageLimit - Warnings: $warnings")
            println("Props: $properties - ChC: $chapterCount")
            println("CrD: $creationDate - LRD: $lastRefreshDate")
            println("WC: $wordCount - isFinished: $isFinished")

            id?.let {
                val storyDescriptor = StoryDescriptor(id = it, author = author, title = title,
                    description = description, category = category, cast = cast,
                    ageLimit = ageLimit, warnings = warnings, properties = properties,
                    chapterCount = chapterCount, creationDate = creationDate,
                    lastRefreshDate = lastRefreshDate, wordCount = wordCount,
                    isFinished = isFinished)
                descriptors.add(storyDescriptor)
            }
        }
    }
}