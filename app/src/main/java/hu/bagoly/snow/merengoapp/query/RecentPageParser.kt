package hu.bagoly.snow.merengoapp.query

import hu.bagoly.snow.merengoapp.model.StoryDescriptor
import org.jsoup.nodes.Document

class RecentPageParser {
    val idRegex = Regex("sid=(\\d+)")
    val descriptors = ArrayList<StoryDescriptor>()
    fun parse(doc: Document) {
        val storyDescriptors = doc.select(".mainnav")
        for (story in storyDescriptors) {
            val titleElement = story.select("span.storytitle a")
            val title = titleElement.text()
            val id = idRegex.find(titleElement.attr("href"))?.groups?.get(1)?.value

            val firstRowLinks = story.select("div").get(0).select("> a")
            val author = firstRowLinks.get(0).text()
            val criticsCount = firstRowLinks.get(2).text()

            val description = story.select("tr").get(1).select("td").html()

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

            id?.let {
                val storyDescriptor = StoryDescriptor(
                    id = it, author = author, title = title,
                    description = description, category = category, cast = cast,
                    ageLimit = ageLimit, warnings = warnings, properties = properties,
                    chapterCount = chapterCount, creationDate = creationDate,
                    lastRefreshDate = lastRefreshDate, wordCount = wordCount,
                    isFinished = isFinished
                )
                descriptors.add(storyDescriptor)
            }
        }
    }
}