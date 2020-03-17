package hu.bagoly.snow.merengoapp.hu.bagoly.snow.merengoapp.query

import android.net.ConnectivityManager
import android.net.Network

const val ERROR = -1
const val CONNECT_SUCCESS = 0
const val GET_INPUT_STREAM_SUCCESS = 1
const val PROCESS_INPUT_STREAM_IN_PROGRESS = 2
const val PROCESS_INPUT_STREAM_SUCCESS = 3

class DownloadCancellerNetworkCallback(val nwFragment: NetworkFragment) : ConnectivityManager.NetworkCallback() {
    override fun onLost(network: Network) {
        super.onLost(network)
        nwFragment.cancelDownload()
    }

    override fun onUnavailable() {
        super.onUnavailable()
        nwFragment.cancelDownload()
    }
}

interface DownloadCallback<T> {

    /**
     * Indicates that the callback handler needs to update its appearance or information based on
     * the result of the task. Expected to be called from the main thread.
     */
    fun updateFromDownload(result: T?)

    /**
     * Indicate to callback handler any progress update.
     * @param progressCode must be one of the constants defined in DownloadCallback.Progress.
     * @param percentComplete must be 0-100.
     */
    fun onProgressUpdate(progressCode: Int, percentComplete: Int)

    /**
     * Indicates that the download operation has finished. This method is called even if the
     * download hasn't completed successfully.
     */
    fun finishDownloading()
}