package hu.bagoly.snow.merengoapp

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import hu.bagoly.snow.merengoapp.query.*
import org.jsoup.nodes.Document

abstract class DownloadCallbackActivity : AppCompatActivity(), DownloadCallback<DownloadResponse> {
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

    abstract fun handleResult(doc: Document, refreshType: RefreshType)

    override fun updateFromDownload(result: DownloadResponse?) {
        result?.let { handleResult(it.document, it.refreshType) }
        if (result == null) {
            Toast.makeText(this, "A letöltés sikertelen", Toast.LENGTH_SHORT).show()
        }
        finishDownloading()
    }

    fun startDownloading(url: String, refreshType: RefreshType) {
        if (!downloading) {
            downloading = true
            networkFragment?.startDownload(url, refreshType)

            findViewById<SwipeRefreshLayout>(R.id.swipe_container)?.let {
                it.isRefreshing = true
            }
        }
    }

    override fun finishDownloading() {
        downloading = false
        networkFragment?.cancelDownload()

        findViewById<SwipeRefreshLayout>(R.id.swipe_container)?.let {
            it.isRefreshing = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.toolbar_menu_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}