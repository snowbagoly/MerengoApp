package hu.bagoly.snow.merengoapp

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.recyclerview.widget.RecyclerView
import hu.bagoly.snow.merengoapp.model.ChapterDescriptor
import hu.bagoly.snow.merengoapp.query.ChapterPageParser
import hu.bagoly.snow.merengoapp.query.RefreshType
import hu.bagoly.snow.merengoapp.query.StoryPageParser
import hu.bagoly.snow.merengoapp.query.isStoryPage
import kotlinx.android.synthetic.main.story_descriptor_item_list.*
import kotlinx.android.synthetic.main.story_descriptor_item_list_content.view.*
import org.jsoup.nodes.Document

class StoryActivity : DownloadCallbackActivity() {

    var id: String? = null
    var forceOpenChapter: Boolean = false

    override fun handleResult(doc: Document, refreshType: RefreshType) {
        if (isStoryPage(doc)) {
            val parser = StoryPageParser()
            parser.parse(doc)
            val layout = findViewById<ViewFlipper>(R.id.content_view_container)
            layout.displayedChild = 1
            val title = findViewById<TextView>(R.id.title)
            println("title: ${parser.title}, chapterTitle: ${parser.currentChapterTitle}")
            title.text = parser.title

            val content = findViewById<TextView>(R.id.content)
            content.text = Html.fromHtml(parser.content, Html.FROM_HTML_MODE_COMPACT)
        } else {
            val parser = ChapterPageParser()
            parser.parse(doc)
            val layout = findViewById<ViewFlipper>(R.id.content_view_container)
            layout.displayedChild = 0
            story_descriptor_item_list.adapter =
                ChapterDescriptorRecyclerViewAdapter(parser.descriptors)
            story_descriptor_item_list.adapter?.notifyDataSetChanged()

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bundle = intent.extras
        if (bundle == null || !bundle.containsKey("id")) {
            Toast.makeText(
                applicationContext,
                "Hiba! A történet/fejezet azonosítója nem található!",
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

        id = bundle.getString("id")
        forceOpenChapter = bundle.containsKey("forceOpenChapter")

        initializeNetworkFragment()
    }

    override fun onStart() {
        super.onStart()
        val storyUrl =
            "https://fanfic.hu/merengo/viewstory.php?sid=$id" + if (forceOpenChapter) "&i=1" else ""
        startDownloading(storyUrl, RefreshType.LOAD_NEW)
    }

    class ChapterDescriptorRecyclerViewAdapter(private val values: List<ChapterDescriptor>) :
        RecyclerView.Adapter<ChapterDescriptorRecyclerViewAdapter.ViewHolder>() {
        override fun getItemCount() = values.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.story_descriptor_item_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.idView.text = "${position + 1}. ${item.title} (${item.id}) írta: ${item.author}"
            holder.contentView.text = Html.fromHtml(item.description, Html.FROM_HTML_MODE_COMPACT)
            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, StoryActivity::class.java).apply {
                    putExtra("id", item.id)
                    putExtra("forceOpenChapter", true)
                }
                holder.itemView.context.startActivity(intent)
            }
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val idView: TextView = view.id_text
            val contentView: TextView = view.content
        }
    }

}