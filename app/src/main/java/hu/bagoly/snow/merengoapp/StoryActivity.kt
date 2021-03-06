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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class StoryActivity : DownloadCallbackActivity() {

    var id: String? = null
    var forceOpenChapter: Boolean = false
    var doc: Document? = null

    override fun handleResult(doc: Document, refreshType: RefreshType) {
        this.doc = doc
        if (isStoryPage(doc)) {
            val parser = StoryPageParser()
            parser.parse(doc)
            title = parser.title
            val layout = findViewById<ViewFlipper>(R.id.content_view_container)
            layout.displayedChild = 1
            val title = findViewById<TextView>(R.id.title)
            title.text = "${parser.author}: ${parser.currentChapterTitle}"

            val content = findViewById<TextView>(R.id.content)
            content.text = Html.fromHtml(parser.content, Html.FROM_HTML_MODE_COMPACT)
        } else {
            val parser = ChapterPageParser()
            parser.parse(doc)
            title = parser.storyTitle
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

        // disabling swipe action (keeping the swipe refresh layout, as we use the fancy animation)
        swipe_container.isEnabled = false

        // loading everything from saved state when it exists
        val bundle = if (savedInstanceState != null) savedInstanceState else intent.extras
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
        forceOpenChapter =
            bundle.containsKey("forceOpenChapter") && bundle.getBoolean("forceOpenChapter")
        if (bundle.containsKey("doc")) {
            handleResult(Jsoup.parse(bundle.getString("doc")), RefreshType.LOAD_NEW)
        }

        initializeNetworkFragment()
    }

    override fun onStart() {
        super.onStart()

        // Load the story/chapters only when necessary
        if (doc == null) {
            val storyUrl =
                "https://fanfic.hu/merengo/viewstory.php?sid=$id" + if (forceOpenChapter) "&i=1" else ""
            startDownloading(storyUrl, RefreshType.LOAD_NEW)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (doc != null) {
            outState.putString("doc", doc.toString())
        }

        outState.putString("id", id)
        outState.putBoolean("forceOpenChapter", forceOpenChapter)
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
            holder.idView.text = "${position + 1}. ${item.author}: ${item.title}"
            holder.contentView.text = Html.fromHtml(item.description, Html.FROM_HTML_MODE_COMPACT)
            holder.additionalDetailsView.text = Html.fromHtml(
                "Korhatár: ${item.ageLimit}<br>" +
                        "Figyelmeztetések: <b>${item.warnings}</b>",
                Html.FROM_HTML_MODE_COMPACT
            )

            holder.idView.setOnClickListener {
                val intent = Intent(holder.itemView.context, StoryActivity::class.java).apply {
                    putExtra("id", item.id)
                    putExtra("forceOpenChapter", true)
                }
                holder.itemView.context.startActivity(intent)
            }

            val extendOnClickListener = View.OnClickListener {
                val toExtend = holder.contentView.maxLines < Integer.MAX_VALUE
                if (toExtend) {
                    holder.contentView.maxLines = Integer.MAX_VALUE
                    holder.additionalDetailsView.visibility = View.VISIBLE
                    holder.extendArrowView.text = "▲"
                } else {
                    holder.contentView.maxLines = 3
                    holder.additionalDetailsView.visibility = View.GONE
                    holder.extendArrowView.text = "▼"
                }
            }
            holder.contentView.setOnClickListener(extendOnClickListener)
            holder.additionalDetailsView.setOnClickListener(extendOnClickListener)
            holder.extendArrowView.setOnClickListener(extendOnClickListener)
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val idView: TextView = view.id_text
            val contentView: TextView = view.content
            val additionalDetailsView: TextView = view.additional_details
            val extendArrowView: TextView = view.extend_arrow
        }
    }

}