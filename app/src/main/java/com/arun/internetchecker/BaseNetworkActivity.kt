package com.arun.internetchecker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.BaseApplication

/**
 * [BaseNetworkActivity] is an abstract class used to detect network changes. You will
 * observe [onNetworkChanged] method to check for internet changes.
 *
 * <p>[ActiveNetworkTest] is an async-task used to find active internet connection.</p>
 *
 * <p>We are maintaining two states here
 *    ->    one is app-level NetworkState
 *    ->    another one is activity-level NetworkState</p>
 *
 *
 */
abstract class BaseNetworkActivity : AppCompatActivity(), ActiveNetworkTest.Callback {
    /**
     * Manager used to check for network change callbacks.
     */
    private var connMgr: ConnectivityManager? = null
    /**
     * Async-task used to check for active internet connection
     */
    private var mNetworkAsyncTest: ActiveNetworkTest? = null
    /**
     * [NetworkState] of this activity, it is different from application based [NetworkState]
     */
    private var mActivityNetworkState: NetworkState = NetworkState()
    /**
     * Flag used to register network connectivity changes
     * By default it is -> true
     */
    private var mEnableInternetEvents = true

    companion object {
        private const val TAG = "InternetChecker111"
        private const val CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE"

        private fun isInternetConnected(connMgr: ConnectivityManager?): Boolean {
            if (connMgr == null) return false
            if (DeviceInfo.isMarshmallowPlus()) {
                connMgr.let {
                    val netCapabilities = it.getNetworkCapabilities(it.activeNetwork)
                    return netCapabilities != null
                            && (netCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            || netCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                }
            } else {
                return connMgr.activeNetworkInfo?.isConnected == true
            }
        }

        @Suppress("unused")
        private fun isInternetConnected(context: Context?) =
            isInternetConnected((context?.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager?))

        private fun getCurrentNetwork(connMgr: ConnectivityManager?): NetworkState {
            if (connMgr == null || !isInternetConnected(connMgr)) return NetworkState()
            val state = NetworkState()
            if (DeviceInfo.isMarshmallowPlus()) {
                val netCapabilities = connMgr.getNetworkCapabilities(connMgr.activeNetwork)
                    ?: return state
                state.type = if (netCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                    NetworkState.TYPE_WIFI
                else NetworkState.TYPE_MOBILE
                state.isConnected = netCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || netCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            } else {
                val activeInternetInfo = connMgr.activeNetworkInfo
                    ?: return state
                state.isConnected = activeInternetInfo.isConnected
                state.type = if (activeInternetInfo.type == ConnectivityManager.TYPE_WIFI)
                    NetworkState.TYPE_WIFI
                else NetworkState.TYPE_MOBILE
            }
            return state
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BaseApplication.getInstance(this)?.getAppNetworkState()
            ?.let { mActivityNetworkState.type = it.type }
        connMgr = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager?
    }

    override fun onStart() {
        super.onStart()
        register()
    }

    override fun onStop() {
        super.onStop()
        unregister()
        if (isFinishing || isDestroyed) {
            connMgr = null
            cancelNetworkTest()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelNetworkTest()
    }

    private fun register() {
        if (!mEnableInternetEvents) return
        when {
            DeviceInfo.isNougatPlusDevice() -> {
                connMgr?.registerDefaultNetworkCallback(connectivityManagerCallback)
            }
            DeviceInfo.isLollipopPlus() -> {
                connMgr?.registerNetworkCallback(NetworkRequest.Builder().apply {
                    addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                }.build(), connectivityManagerCallback)
            }
            else -> registerReceiver(networkReceiver, IntentFilter(CONNECTIVITY_ACTION))
        }
        // When using "registerNetworkCallback", it doesn't give "onLost" or "onAvailable"
        // while registering, So we've to manually update.
        if (DeviceInfo.isLollipopPlus() && !isInternetConnected(connMgr)) {
            Log.d(TAG, "register -> isConnected:: false")
            checkAndUpdateNetwork(false)
        }
        //////////////////////////////////////////////////////////////////////////////////
    }

    private fun unregister() {
        if (!mEnableInternetEvents) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connMgr?.unregisterNetworkCallback(connectivityManagerCallback)
        } else {
            try {
                unregisterReceiver(networkReceiver)
            } catch (e: IllegalArgumentException) {
            }
        }
    }

    /**
     * Calling this method in onCreate or onResume of child activity will stop registering for ConnectivityManager.CONNECTIVITY_ACTION events
     * It sets mEnableInternetEvents to false
     */
    fun unregisterInternetEvents() {
        this.mEnableInternetEvents = false
    }

    fun isActiveInternet() = BaseApplication.getInstance(this)
        ?.isActiveInternet() == true

    private fun startNetworkTest() {
        cancelNetworkTest()
        mNetworkAsyncTest = ActiveNetworkTest(this).apply {
            execute()
        }
    }

    private fun cancelNetworkTest() {
        mNetworkAsyncTest?.cancel()
        mNetworkAsyncTest = null
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private val connectivityManagerCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.d(TAG, "onAvailable -> isConnected:: true")
            checkAndUpdateNetwork(true)
        }

        override fun onUnavailable() {
            super.onUnavailable()
            Log.d(TAG, "onUnavailable -> isConnected:: false")
            checkAndUpdateNetwork(false)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Log.d(TAG, "onLost -> isConnected:: false")
            checkAndUpdateNetwork(false)
        }

    }

    override fun onNetworkChanged(b: Boolean) {
        Log.d(TAG, "onNetworkChanged -> $b")
        getCurrentNetwork(connMgr).apply { isActive = b }.let {
            BaseApplication.getInstance(this)?.updateNetworkState(it)
            mActivityNetworkState = it
        }

    }

    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "onReceive -> isConnected:: ${isInternetConnected(connMgr)}")
            checkAndUpdateNetwork(isInternetConnected(connMgr))
        }
    }

    private fun checkAndUpdateNetwork(b: Boolean) {
        runOnUiThread {
            cancelNetworkTest()
            if (!b) {
                onNetworkChanged(b)
            } else {
                if (!mActivityNetworkState.isActive
                    || mActivityNetworkState.type != getCurrentNetwork(connMgr).type
                ) {
                    startNetworkTest()
                } else if (BaseApplication.getInstance(this)?.isActiveInternet() == true
                    && !mActivityNetworkState.isActive
                ) {
                    onNetworkChanged(true)
                }
            }
        }
    }

    data class NetworkState(
        var isConnected: Boolean,
        var isActive: Boolean,
        var type: Int = TYPE_UNKNOWN
    ) {
        constructor() : this(false, false, TYPE_UNKNOWN)

        companion object {
            const val TYPE_WIFI = 0
            const val TYPE_MOBILE = 1
            const val TYPE_UNKNOWN = -1
        }
    }
}