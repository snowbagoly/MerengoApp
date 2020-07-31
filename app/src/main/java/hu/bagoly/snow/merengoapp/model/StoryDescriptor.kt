package hu.bagoly.snow.merengoapp.model

class StoryDescriptor(
    val id: String,
    val title: String,
    val author: String,
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
)
{
    fun isAdultContent() = ageLimit == "18"
}