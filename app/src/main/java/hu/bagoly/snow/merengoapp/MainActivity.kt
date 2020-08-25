package hu.bagoly.snow.merengoapp

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import hu.bagoly.snow.merengoapp.model.StoryDescriptor
import hu.bagoly.snow.merengoapp.query.RecentPageParser
import hu.bagoly.snow.merengoapp.query.RefreshType
import kotlinx.android.synthetic.main.story_descriptor_item_list.*
import kotlinx.android.synthetic.main.story_descriptor_item_list_content.view.*
import org.jsoup.nodes.Document

class MainActivity : DownloadCallbackActivity() {

    val parser = RecentPageParser()
    var lastOffsetLoaded: Int? = null
    var triggeredOffset = 0
    val recentPageUrl = "https://fanfic.hu/merengo/search.php?action=recent"

    private fun getRecentPageUrlWithOffset() = recentPageUrl + "&offset=${triggeredOffset}"

    override fun handleResult(doc: Document, refreshType: RefreshType) {
        parser.parse(doc, refreshType)
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
        swipe_container.setOnRefreshListener(StoryListRefreshListener(this::triggerLoadingNext))
    }

    override fun onStart() {
        super.onStart()
        startDownloading(recentPageUrl, RefreshType.LOAD_NEW)
    }

    private fun triggerLoadingNext(refreshType: RefreshType) {
        if (refreshType == RefreshType.LOAD_NEW) {
            startDownloading(recentPageUrl, refreshType)
            return
        }
        // ignore loading if the last trigger was not yet finished
        if (lastOffsetLoaded != triggeredOffset)
            return

        triggeredOffset += 15
        startDownloading(getRecentPageUrlWithOffset(), refreshType)
    }

    class StoryListRefreshListener(val triggerUpdate: (RefreshType) -> Unit) :
        SwipeRefreshLayout.OnRefreshListener {
        override fun onRefresh() {
            triggerUpdate(RefreshType.LOAD_NEW)
        }
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
                val context = holder.itemView.context
                val intent = Intent(context, StoryActivity::class.java).apply {
                    putExtra("id", item.id)
                }

                val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                if (item.isAdultContent() && sharedPref.getBoolean("adult_content_warning", true)) {
                    val dialog = AlertDialog.Builder(context)
                        .setMessage("Tizennyolc éven aluliak számára nem ajánlott.")
                        .setNegativeButton("Mégse", null)
                        .setPositiveButton("Ok") { _, _ ->
                            context.startActivity(intent)
                        }
                        .create()
                    dialog.show()
                } else {
                    context.startActivity(intent)
                }
            }
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val idView: TextView = view.id_text
            val contentView: TextView = view.content
        }
    }

}
