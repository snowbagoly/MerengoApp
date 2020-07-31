package hu.bagoly.snow.merengoapp

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hu.bagoly.snow.merengoapp.model.StoryDescriptor
import hu.bagoly.snow.merengoapp.query.RecentPageParser
import kotlinx.android.synthetic.main.story_descriptor_item_list.*
import kotlinx.android.synthetic.main.story_descriptor_item_list_content.view.*
import org.jsoup.nodes.Document

class MainActivity : DownloadCallbackActivity() {

    val parser = RecentPageParser()
    val recentPageUrl =
        "https://fanfic.hu/merengo/search.php?action=recent&offset=8000" //TODO don't forget to set this back

    override fun handleResult(doc: Document) {
        parser.parse(doc)
        story_descriptor_item_list.adapter?.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeNetworkFragment(recentPageUrl)
        story_descriptor_item_list.adapter = StoryDescriptorRecyclerViewAdapter(parser.descriptors)
    }

    override fun onStart() {
        super.onStart()
        startDownloading()
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
