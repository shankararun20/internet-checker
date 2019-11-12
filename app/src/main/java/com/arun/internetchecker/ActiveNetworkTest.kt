package com.arun.internetchecker

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class ActiveNetworkTest(
    private val callback: Callback? = null
) :
    AsyncTask<Void, Boolean, Boolean>() {
    private var handler: Handler = Handler(Looper.getMainLooper())

    companion object {
        const val TEST_URL = "https://www.google.co.in/"
        private const val TIMEOUT = 8000L
    }

    override fun onPreExecute() {
        super.onPreExecute()
        handler.postDelayed(timeoutRunnable, TIMEOUT)
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        return try {
            val urlCon = URL(TEST_URL).openConnection() as HttpsURLConnection
            urlCon.setRequestProperty("User-Agent", "Test")
            urlCon.setRequestProperty("Connection", "close")
            urlCon.connectTimeout = 10000
            urlCon.connect()
            urlCon.responseCode == HttpsURLConnection.HTTP_OK
        } catch (e: IOException) {
            false
        }
    }

    override fun onPostExecute(result: Boolean?) {
        super.onPostExecute(result)
        if (isCancelled) return
        cancel()
        Log.d("InternetChecker222", "onPostExecute -> ${result ?: false}")
        callback?.onNetworkChanged(result ?: false)
    }

    fun cancel() {
        handler.removeCallbacksAndMessages(null)
        cancel(true)
    }

    private val timeoutRunnable = Runnable {
        if (isCancelled) return@Runnable
        Log.d("InternetChecker222", "timeoutRunnable called")
        onPostExecute(false)
    }

    interface Callback {
        fun onNetworkChanged(b: Boolean)
    }
}