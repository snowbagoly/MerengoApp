package hu.bagoly.snow.merengoapp.query

import hu.bagoly.snow.merengoapp.model.StoryDescriptor
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.*
import kotlin.collections.HashMap

enum class RefreshType {
    LOAD_NEW,
    LOAD_OLD
}

class RecentPageParser {
    private val idRegex = Regex("sid=(\\d+)")
    private val descriptorHelperMap = HashMap<String, StoryDescriptor>()
    val descriptors = LinkedList<StoryDescriptor>()

    fun parse(doc: Document, refreshType: RefreshType = RefreshType.LOAD_NEW) {
        val storyElements = doc.select(".mainnav")

        val newDescriptorsToAdd = ArrayList<StoryDescriptor>()
        for (storyElement in storyElements) {
            val storyDescriptor = parseRawData(storyElement) ?: continue

            // if the element is not updated in place, it has to be added to the beginning/end of the list in order
            if (!handleDuplicate(storyDescriptor, refreshType)) {
                newDescriptorsToAdd.add(storyDescriptor)
            }
            descriptorHelperMap[storyDescriptor.id] = storyDescriptor
        }

        when (refreshType) {
            RefreshType.LOAD_NEW -> descriptors.addAll(0, newDescriptorsToAdd)
            RefreshType.LOAD_OLD -> descriptors.addAll(newDescriptorsToAdd)
        }
    }

    /**
     * Returns whether or not the newDescriptor was already added to the list
     */
    private fun handleDuplicate(newDescriptor: StoryDescriptor, refreshType: RefreshType): Boolean {
        if (!descriptorHelperMap.containsKey(newDescriptor.id))
            return false
        val oldDescriptor = descriptorHelperMap[newDescriptor.id]
        val oldDescriptorIndex = descriptors.indexOf(oldDescriptor)
        if (oldDescriptorIndex < 0) {
            // invalid list format, the element was removed from the list already (or not yet added)
            // ignoring error, the key will be updated anyway
            return false
        }

        descriptors.removeAt(oldDescriptorIndex)

        // the old element is changed in place, but the new has to be added at the top in order
        return when (refreshType) {
            RefreshType.LOAD_NEW -> false
            RefreshType.LOAD_OLD -> {
                descriptors.add(oldDescriptorIndex, newDescriptor)
                true
            }
        }
    }

    private fun parseRawData(story: Element): StoryDescriptor? {
        val titleElement = story.select("span.storytitle a")
        val title = titleElement.text()
        val id = idRegex.find(titleElement.attr("href"))?.groups?.get(1)?.value

        val firstRowLinks = story.select("div").get(0).select("> a")
        val author = firstRowLinks.get(0).text()
        val criticsCount = firstRowLinks.get(2).text()

        val rawDescription = story.select("tr").get(1).select("td").text()
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
            return StoryDescriptor(
                id = it, author = author, title = title, rawDescription = rawDescription,
                description = description, category = category, cast = cast,
                ageLimit = ageLimit, warnings = warnings, properties = properties,
                chapterCount = chapterCount, creationDate = creationDate,
                lastRefreshDate = lastRefreshDate, wordCount = wordCount,
                isFinished = isFinished
            )
        }
        return null
    }
}