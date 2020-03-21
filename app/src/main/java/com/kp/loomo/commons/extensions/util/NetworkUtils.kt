package com.kp.loomo.commons.extensions.util

import android.net.ConnectivityManager
import android.net.NetworkInfo

object NetworkUtils {
    /**
     * Check if internet connection available
     */

    fun hasInternetConnection(connectivityManager : ConnectivityManager): Boolean {
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }
}