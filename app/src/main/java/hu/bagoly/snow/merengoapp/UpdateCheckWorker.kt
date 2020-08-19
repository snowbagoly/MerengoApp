package hu.bagoly.snow.merengoapp

import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import hu.bagoly.snow.merengoapp.model.StoryDescriptor
import hu.bagoly.snow.merengoapp.query.RecentPageParser
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException


class UpdateCheckWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    val recentPageUrl = "https://fanfic.hu/merengo/search.php?action=recent"

    private fun compareDocs(
        referenceDoc: Document,
        latestDoc: Document
    ): List<StoryDescriptor> {
        val newStories = ArrayList<StoryDescriptor>()

        val referenceParser = RecentPageParser()
        referenceParser.parse(referenceDoc)
        val latestParser = RecentPageParser()
        latestParser.parse(latestDoc)

        for (freshStory in latestParser.descriptors) {
            val sameOldStory =
                referenceParser.descriptors.find { oldStory -> oldStory.id == freshStory.id }
            if (sameOldStory == null ||
                sameOldStory.wordCount != freshStory.wordCount ||
                sameOldStory.chapterCount != freshStory.chapterCount
            ) {
                newStories.add(freshStory)
            }
        }
        return newStories
    }

    private fun createNotification(newStories: List<StoryDescriptor>) {
        val freshStoryTitles = newStories.take(3).joinToString("\n") { it.title }
        val notificationText =
            "A következő " +
                    (if (newStories.count() == 1) "történetből" else "történetekből") +
                    " van friss:\n" + freshStoryTitles +
                    (if (newStories.count() <= 3) "" else "\n... és ez még nem minden!")

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(applicationContext, 0, intent, 0)

        val builder = NotificationCompat.Builder(applicationContext, "MerengoApp")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Friss a Merengőn!")
            .setContentText(notificationText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(notificationText)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(applicationContext)) {
            // constant id is used, because we want to rewrite any existing notification
            notify(0, builder.build())
        }
    }

    private fun updateDatabase(database: SQLiteDatabase, latestDoc: String) {
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

    }

    private fun downloadLatest(): Document? {
        return try {
            Jsoup.connect(recentPageUrl).get()
        } catch (ex: IOException) {
            null
        }
    }

    override fun doWork(): Result {
        val latestDoc = downloadLatest() ?: return Result.failure()

        val database =
            applicationContext.openOrCreateDatabase("MerengoApp", Context.MODE_PRIVATE, null)

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

        var needsRefresh = false

        if (!cursor.isAfterLast) {
            val previousDoc = Jsoup.parse(cursor.getString(cursor.getColumnIndex("doc")))
            val freshStories: List<StoryDescriptor> = compareDocs(previousDoc, latestDoc)
            if (freshStories.isNotEmpty()) {
                createNotification(freshStories)
                needsRefresh = true
            }
        }

        if (needsRefresh) {
            updateDatabase(database, latestDoc.toString())
        }

        database.close()

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }
}