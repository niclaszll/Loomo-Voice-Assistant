package com.kp.loomo.features.intents.handler

import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler

private const val TAG = "OnlineTestHandler"

class OnlineTestHandler constructor(private var connectivityManager: ConnectivityManager) :
    IntentMessageHandler {

    private val keywords = arrayOf("online", "offline", "internet")

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        return intentMessage.queryResult.intent.displayName == "OnlineTest"
    }

    override fun handle(intentMessage: DetectIntentResponse): String {
        return if (hasInternetConnection()) {
            "I am currently online."
        } else {
            "I am currently offline."
        }
    }

    override fun canHandleOffline(intentMessage: String): Boolean {
        for (keyword in keywords) {
            if (intentMessage.contains(keyword, true)) {
                return true
            }
        }
        return false
    }

    override fun handleOffline(intentMessage: String): String {
        return if (hasInternetConnection()) {
            "I am currently online."
        } else {
            "I am currently offline."
        }
    }

    /**
     * Check if internet connection available
     */

    private fun hasInternetConnection(): Boolean {
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }
}