package hu.bagoly.snow.merengoapp

import android.content.Context
import android.net.ConnectivityManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import hu.bagoly.snow.merengoapp.query.DownloadCallback
import hu.bagoly.snow.merengoapp.query.DownloadCancellerNetworkCallback
import hu.bagoly.snow.merengoapp.query.NetworkFragment
import org.jsoup.nodes.Document

abstract class DownloadCallbackActivity : AppCompatActivity(), DownloadCallback<Document> {
    // Keep a reference to the NetworkFragment, which owns the AsyncTask object
    // that is used to execute network ops.
    private var networkFragment: NetworkFragment? = null

    // Boolean telling us whether a download is in progress, so we don't trigger overlapping
    // downloads with consecutive button clicks.
    private var downloading = false

    fun initializeNetworkFragment() {
        networkFragment = NetworkFragment.getInstance(supportFragmentManager)
        networkFragment?.let {
            val networkCallback = DownloadCancellerNetworkCallback(it)
            val connectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        }
    }

    abstract fun handleResult(doc: Document)

    override fun updateFromDownload(result: Document?) {
        result?.let { handleResult(it) }
        if (result == null) {
            Toast.makeText(this, "A letöltés sikertelen", Toast.LENGTH_SHORT).show()
        }
        finishDownloading()
    }

    fun startDownloading(url: String) {
        if (!downloading) {
            downloading = true
            networkFragment?.startDownload(url)
        }
    }

    override fun finishDownloading() {
        downloading = false
        networkFragment?.cancelDownload()
    }
}