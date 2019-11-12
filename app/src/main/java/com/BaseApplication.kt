package com

import android.app.Application
import android.content.Context
import com.arun.internetchecker.BaseNetworkActivity

class BaseApplication : Application() {

    companion object {
        fun getInstance(context: Context?) = (context?.applicationContext as BaseApplication?)
    }

    private var mNetworkState: BaseNetworkActivity.NetworkState = BaseNetworkActivity.NetworkState()

    fun isActiveInternet() = mNetworkState.isActive

    fun getAppNetworkState() = mNetworkState

    fun updateNetworkState(network: BaseNetworkActivity.NetworkState) {
        this.mNetworkState = network
    }

}