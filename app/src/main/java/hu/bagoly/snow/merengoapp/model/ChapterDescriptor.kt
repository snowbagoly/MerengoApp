package hu.bagoly.snow.merengoapp.model

class ChapterDescriptor(
    val id: String,
    val title: String,
    val author: String,
    val warnings: String,
    val wordCount: String,
    val ageLimit: String,
    val description: String
)
{
    fun isAdultContent() = ageLimit == "18"
}