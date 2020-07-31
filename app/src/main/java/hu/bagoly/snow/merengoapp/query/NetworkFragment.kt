package hu.bagoly.snow.merengoapp.query

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException

private const val TAG = "NetworkFragment"
private const val URL_KEY = "UrlKey"

class NetworkFragment : Fragment() {
    private var callback: DownloadCallback<Document>? = null
    private var downloadTask: DownloadTask? = null

    companion object {
        /**
         * Static initializer for NetworkFragment that sets the URL of the host it will be
         * downloading from.
         */
        fun getInstance(fragmentManager: FragmentManager, url: String): NetworkFragment {
            // Recover NetworkFragment in case we are re-creating the Activity due to a config change.
            // This is necessary because NetworkFragment might have a task that began running before
            // the config change occurred and has not finished yet.
            // The NetworkFragment is recoverable because it calls setRetainInstance(true).
            var networkFragment = fragmentManager.findFragmentByTag(TAG) as? NetworkFragment
            if (networkFragment == null) {
                networkFragment = NetworkFragment()
                networkFragment.arguments = Bundle().apply {
                    putString(URL_KEY, url)
                }
                fragmentManager.beginTransaction()
                    .add(networkFragment, TAG)
                    .commit()
            }
            return networkFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retain this Fragment across configuration changes in the host Activity.
        retainInstance = true
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        // Host Activity will handle callbacks from task.
        callback = context as? DownloadCallback<Document>
    }

    override fun onDetach() {
        super.onDetach()
        // Clear reference to host Activity to avoid memory leak.
        callback = null
    }

    override fun onDestroy() {
        // Cancel task when Fragment is destroyed.
        cancelDownload()
        super.onDestroy()
    }

    /**
     * Start non-blocking execution of DownloadTask.
     */
    fun startDownload() {
        cancelDownload()
        callback?.also {
            downloadTask = DownloadTask(it).apply {
                val urlString = arguments?.getString(URL_KEY)
                execute(urlString)
            }
        }
    }

    /**
     * Cancel (and interrupt if necessary) any ongoing DownloadTask execution.
     */
    fun cancelDownload() {
        downloadTask?.cancel(true)
    }

    /**
     * Implementation of AsyncTask designed to fetch data from the network.
     */
    private class DownloadTask(callback: DownloadCallback<Document>)
        : AsyncTask<String, Int, DownloadTask.Result>() {

        private var callback: DownloadCallback<Document>? = null

        init {
            setCallback(callback)
        }

        internal fun setCallback(callback: DownloadCallback<Document>) {
            this.callback = callback
        }

        /**
         * Wrapper class that serves as a union of a result value and an exception. When the download
         * task has completed, either the result value or exception can be a non-null value.
         * This allows you to pass exceptions to the UI thread that were thrown during doInBackground().
         */
        internal class Result {
            var resultValue: Document? = null
            var exception: Exception? = null

            constructor(resultValue: Document) {
                this.resultValue = resultValue
            }

            constructor(exception: Exception) {
                this.exception = exception
            }
        }

        /**
         * Cancel background network operation if we do not have network connectivity.
         */
        override fun onPreExecute() {
        }

        /**
         * Defines work to perform on the background thread.
         */
        override fun doInBackground(vararg urls: String): DownloadTask.Result? {
            var result: Result? = null
            if (!isCancelled && urls.isNotEmpty()) {
                val urlString = urls[0]
                result = try {
                    val resultString = Jsoup.connect(urlString).get()
                    if (resultString != null) {
                        Result(resultString)
                    } else {
                        throw IOException("No response received.")
                    }
                } catch (e: Exception) {
                    Result(e)
                }

            }
            return result
        }

        /**
         * Updates the DownloadCallback with the result.
         */
        override fun onPostExecute(result: Result?) {
            callback?.apply {
                result?.exception?.also { exception ->
                    //updateFromDownload(exception.message)
                    updateFromDownload(null)
                    return
                }
                result?.resultValue?.also { resultValue ->
                    updateFromDownload(resultValue)
                    return
                }
                finishDownloading()
            }
        }

        /**
         * Override to add special behavior for cancelled AsyncTask.
         */
        override fun onCancelled(result: Result) {}
    }

}