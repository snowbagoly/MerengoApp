package hu.bagoly.snow.merengoapp.query

import android.net.ConnectivityManager
import android.net.Network

class DownloadCancellerNetworkCallback(val nwFragment: NetworkFragment) :
    ConnectivityManager.NetworkCallback() {
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
     * Indicates that the download operation has finished. This method is called even if the
     * download hasn't completed successfully.
     */
    fun finishDownloading()
}