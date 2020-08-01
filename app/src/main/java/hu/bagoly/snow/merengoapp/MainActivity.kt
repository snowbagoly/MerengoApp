package hu.bagoly.snow.merengoapp

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hu.bagoly.snow.merengoapp.model.StoryDescriptor
import hu.bagoly.snow.merengoapp.query.RecentPageParser
import hu.bagoly.snow.merengoapp.query.RefreshType
import kotlinx.android.synthetic.main.story_descriptor_item_list.*
import kotlinx.android.synthetic.main.story_descriptor_item_list_content.view.*
import org.jsoup.nodes.Document

class MainActivity : DownloadCallbackActivity() {

    val parser = RecentPageParser()
    var lastRefreshType: RefreshType = RefreshType.LOAD_NEW
    var lastOffsetLoaded: Int? = null
    var triggeredOffset = 0
    val recentPageUrl = "https://fanfic.hu/merengo/search.php?action=recent"

    override fun handleResult(doc: Document) {
        parser.parse(doc, lastRefreshType)
        story_descriptor_item_list.adapter?.notifyDataSetChanged()
        lastOffsetLoaded = triggeredOffset
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeNetworkFragment()
        story_descriptor_item_list.adapter = StoryDescriptorRecyclerViewAdapter(parser.descriptors)
        story_descriptor_item_list.layoutManager = LinearLayoutManager(this)
        story_descriptor_item_list.addOnScrollListener(StoryListScrollListener(this::triggerLoadingNext))
    }

    override fun onStart() {
        super.onStart()
        startDownloading(recentPageUrl)
    }

    fun triggerLoadingNext(refreshType: RefreshType) {
        // ignore loading if the last trigger was not yet finished
        if (lastOffsetLoaded != triggeredOffset)
            return

        triggeredOffset += 15
        lastRefreshType = refreshType // TODO this will not work properly
        val recentPageUrl =
            "https://fanfic.hu/merengo/search.php?action=recent&offset=${triggeredOffset}" // check if it changes or not
        startDownloading(recentPageUrl)
    }

    class StoryListScrollListener(val triggerUpdate: (RefreshType) -> Unit) :
        RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val totalItemCount = recyclerView.layoutManager!!.itemCount
            val layoutManager: LinearLayoutManager =
                recyclerView.layoutManager as LinearLayoutManager
            val lastItemPosition = layoutManager.findLastVisibleItemPosition()
            if (lastItemPosition == totalItemCount - 1) {
                triggerUpdate(RefreshType.LOAD_OLD)
            }
        }
    }

    class StoryDescriptorRecyclerViewAdapter(private val values: List<StoryDescriptor>) :
        RecyclerView.Adapter<StoryDescriptorRecyclerViewAdapter.ViewHolder>() {
        override fun getItemCount() = values.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.story_descriptor_item_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.idView.text = "${item.title} (${item.id}) írta: ${item.author}"
            holder.contentView.text = Html.fromHtml(item.description, Html.FROM_HTML_MODE_COMPACT)
            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, StoryActivity::class.java).apply {
                    putExtra("id", item.id)
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
