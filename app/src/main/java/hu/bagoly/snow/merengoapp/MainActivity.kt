package hu.bagoly.snow.merengoapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.*
import hu.bagoly.snow.merengoapp.model.StoryDescriptor
import hu.bagoly.snow.merengoapp.query.RecentPageParser
import hu.bagoly.snow.merengoapp.query.RefreshType
import kotlinx.android.synthetic.main.story_descriptor_item_list.*
import kotlinx.android.synthetic.main.story_descriptor_item_list_content.view.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.concurrent.TimeUnit

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
        insertIntoDatabase(doc.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeNotificationChannel()
        initializeNetworkFragment()
        story_descriptor_item_list.adapter = StoryDescriptorRecyclerViewAdapter(parser.descriptors)
        story_descriptor_item_list.layoutManager = LinearLayoutManager(this)
        story_descriptor_item_list.addOnScrollListener(StoryListScrollListener(this::triggerLoadingNext))
        swipe_container.setOnRefreshListener(StoryListRefreshListener(this::triggerLoadingNext))

        loadCacheFromDatabase()
        initializeUpdateCheckWorker()
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

    private fun insertIntoDatabase(latestDoc: String) {
        val database =
            applicationContext.openOrCreateDatabase("MerengoApp", Context.MODE_PRIVATE, null)
        database.execSQL("CREATE TABLE IF NOT EXISTS most_recent_data (doc VARCHAR, insertion_date DATE default CURRENT_DATE)")
        val contentValues = ContentValues()
        contentValues.put("doc", latestDoc)
        database.beginTransaction()
        try {
            // In case the lack of thread safety messed up the database, cleaning it up
            database.delete("most_recent_data", null, null)
            // Inserting the most recent data
            database.insert("most_recent_data", null, contentValues)
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
        database.close()
    }

    private fun loadCacheFromDatabase() {
        val database =
            applicationContext.openOrCreateDatabase("MerengoApp", Context.MODE_PRIVATE, null)
        database.execSQL("CREATE TABLE IF NOT EXISTS most_recent_data (doc VARCHAR, insertion_date DATE default CURRENT_DATE)")
        val cursor = database.query(
            "most_recent_data",
            arrayOf("doc"),
            null,
            null,
            null,
            null,
            "insertion_date DESC",
            "1"
        )
        cursor.moveToFirst()
        if (!cursor.isAfterLast) {
            handleResult(
                Jsoup.parse(cursor.getString(cursor.getColumnIndex("doc"))),
                RefreshType.LOAD_NEW
            )
        }
        database.close()
    }

    private fun initializeNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "MerengoApp"
            val channelDescription = "Pushing notifications if new content is uploaded to Merengő"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                "MerengoApp",
                channelName,
                importance
            ).apply {
                description = channelDescription
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun initializeUpdateCheckWorker() {
        val workRequest =
            PeriodicWorkRequestBuilder<UpdateCheckWorker>(30, TimeUnit.MINUTES)
                .addTag("update_check")
                .setInitialDelay(15, TimeUnit.MINUTES)
                .build()
        val workManager = WorkManager.getInstance(this)
        workManager.cancelAllWorkByTag("update_check")
        workManager.pruneWork()
        workManager.enqueueUniquePeriodicWork(
            "update_check",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
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
