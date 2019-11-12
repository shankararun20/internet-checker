package com.arun.internetchecker

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_main.*

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class MainActivity : BaseNetworkActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onNetworkChanged(b: Boolean) {
        super.onNetworkChanged(b)
        txt?.text = if (b) "Internet Connected" else "Internet disconnected"
    }

}
