package com.koalatea.sedaily.network

import android.content.Context
import com.koalatea.sedaily.util.isConnected

class NetworkManager(private val applicationContext: Context) {

    val isConnected: Boolean
        get() = applicationContext.isConnected

}