package hu.bagoly.snow.merengoapp.query

import hu.bagoly.snow.merengoapp.model.ChapterDescriptor
import org.jsoup.nodes.Document

class ChapterPageParser {
    val descriptors = ArrayList<ChapterDescriptor>()
    val idRegex = Regex("sid=(\\d+)")

    fun parse(doc: Document) {
        val rawChapterDescriptors = doc.select(".mainnav tr")

        // a chapter descriptor consists of five rows
        for (i in 0 until rawChapterDescriptors.size step 5) {
            val titleElement = rawChapterDescriptors.get(i + 1).select("span.storytitle a")
            val title = titleElement.text()
            val id = idRegex.find(titleElement.attr("href"))?.groups?.get(1)?.value

            val firstRowLinks = rawChapterDescriptors.get(i + 1).select("div").get(0).select("> a")
            val author = firstRowLinks.get(0).text()
            val criticsCount = firstRowLinks.get(2).text()

            val warnings = rawChapterDescriptors.get(i + 2).select("td").get(1).text()

            val wordCount = rawChapterDescriptors.get(i + 3).select("td").get(1).text()
            val ageLimit = rawChapterDescriptors.get(i + 3).select("td").get(3).text()


            val description = rawChapterDescriptors.get(i + 4).select("td").get(1).html()
            id?.let {
                descriptors.add(
                    ChapterDescriptor(
                        id,
                        title,
                        author,
                        warnings,
                        wordCount,
                        ageLimit,
                        description
                    )
                )
            }
        }
    }
}