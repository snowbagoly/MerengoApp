package hu.bagoly.snow.merengoapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.*

class UpdateCheckWorker(val appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    val recentPageUrl = "https://fanfic.hu/merengo/search.php?action=recent"

//    private fun getDateTime(): String? {
//        val dateFormat = SimpleDateFormat(
//            "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
//        )
//        val date = Date()
//        return dateFormat.format(date)
//    }

    private fun updateDatabase(database: SQLiteDatabase, latestDoc: String) {
        val contentValues = ContentValues()
        contentValues.put("doc", latestDoc)
        // In case the lack of thread safety messed up the database, cleaning it up
        database.delete("most_recent_data", null, null)
        // Inserting the most recent data
        database.insert("most_recent_data", null, contentValues)
    }

    private fun downloadLatest(): String? {
        return null
    }

    override fun doWork(): Result {
        //TODO download latest doc
        println("!!!!!!!!!!Hi I'm here")
        val latestDoc = downloadLatest()

        if (latestDoc == null) {
            println("----------------very failed")
            return Result.failure()
        }

        val database = appContext.openOrCreateDatabase("MerengoApp", Context.MODE_PRIVATE, null)

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
//        val cursor = database.rawQuery("SELECT * FROM most_recent_data", null)
        cursor.moveToFirst()

        var needsRefresh = cursor.isAfterLast()

        if (!cursor.isAfterLast()) {
            println(cursor.getString(cursor.getColumnIndex("doc")))

            //TODO compare two docs
            //TODO send notification if in the latest we have anything new

        }

        if (needsRefresh) {
            updateDatabase(database, latestDoc)
        }

        database.close()

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }
}