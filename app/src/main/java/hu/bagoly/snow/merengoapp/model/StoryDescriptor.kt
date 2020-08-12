package hu.bagoly.snow.merengoapp.model

class StoryDescriptor(
    val id: String,
    val title: String,
    val author: String,
    val rawDescription: String,
    val description: String,
    val category: String,
    val cast: String,
    val ageLimit: String,
    val warnings: String,
    val properties: String,
    val chapterCount: String,
    val creationDate: String,
    val lastRefreshDate: String,
    val wordCount: String,
    val isFinished: Boolean
) {
    fun isAdultContent() = ageLimit == "18"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as StoryDescriptor

        if (id != other.id || title != other.title ||
            author != other.author || rawDescription != other.rawDescription ||
            category != other.category || cast != other.cast ||
            ageLimit != other.ageLimit || warnings != other.warnings ||
            properties != other.properties || chapterCount != other.chapterCount ||
            creationDate != other.creationDate || lastRefreshDate != other.lastRefreshDate ||
            wordCount != other.wordCount || isFinished != other.isFinished) return false

        return true
    }

    override fun hashCode(): Int{
        return id.hashCode() + title.hashCode() + author.hashCode()
    }
}