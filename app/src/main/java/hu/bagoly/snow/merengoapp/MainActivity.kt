package hu.bagoly.snow.merengoapp

import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hu.bagoly.snow.merengoapp.hu.bagoly.snow.merengoapp.model.StoryDescriptor
import hu.bagoly.snow.merengoapp.hu.bagoly.snow.merengoapp.query.*
import kotlinx.android.synthetic.main.story_descriptor_item_list.*
import kotlinx.android.synthetic.main.story_descriptor_item_list_content.view.*
import org.jsoup.nodes.Document

class MainActivity : AppCompatActivity(), DownloadCallback<Document> {
    val parser = RecentPageParser()
    val recentPageUrl = "https://fanfic.hu/merengo/search.php?action=recent"

    // Keep a reference to the NetworkFragment, which owns the AsyncTask object
    // that is used to execute network ops.
    private var networkFragment: NetworkFragment? = null
    // Boolean telling us whether a download is in progress, so we don't trigger overlapping
    // downloads with consecutive button clicks.
    private var downloading = false


    override fun updateFromDownload(result: Document?) {
        result?.let {
            parser.parse(it)
            val vg = findViewById<RecyclerView>(R.id.story_descriptor_item_list)
            story_descriptor_item_list.adapter?.notifyDataSetChanged()
        }
        //TODO if result is null, show info on it

    }


    override fun onProgressUpdate(progressCode: Int, percentComplete: Int) {
        when (progressCode) {
            // You can add UI behavior for progress updates here.
            ERROR -> {

            }
            CONNECT_SUCCESS -> {
            }
            GET_INPUT_STREAM_SUCCESS -> {
            }
            PROCESS_INPUT_STREAM_IN_PROGRESS -> {
            }
            PROCESS_INPUT_STREAM_SUCCESS -> {
            }
        }
    }

    override fun finishDownloading() {
        downloading = false
        networkFragment?.cancelDownload()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        networkFragment = NetworkFragment.getInstance(supportFragmentManager, recentPageUrl)
        networkFragment?.let {
            val networkCallback = DownloadCancellerNetworkCallback(it)
            val connectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        }
        story_descriptor_item_list.adapter = StoryDescriptorRecyclerViewAdapter(parser.descriptors)
    }

    override fun onStart() {
        super.onStart()
        networkFragment?.startDownload()
    }

    class StoryDescriptorRecyclerViewAdapter(private val values: List<StoryDescriptor>) :
        RecyclerView.Adapter<StoryDescriptorRecyclerViewAdapter.ViewHolder>()
    {
        override fun getItemCount() = values.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.story_descriptor_item_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.idView.text = "${item.title} (${item.id}) írta: ${item.author}"
            holder.contentView.text = item.description
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val idView: TextView = view.id_text
            val contentView: TextView = view.content
        }
    }

}
