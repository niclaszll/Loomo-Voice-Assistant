package com.kp.loomo.features.intents.handler

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler
import java.text.DateFormat
import java.util.*

private const val TAG = "WeatherHandler"

class WeatherHandler : IntentMessageHandler {

    private val keywords = arrayOf("weather", "rain")

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        return intentMessage.queryResult.intent.displayName == "Weather"
    }

    override fun handle(intentMessage: DetectIntentResponse): String {

        return "There is no internet connection at the present time. Please try later again."
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

        return "There is no internet connection at the present time. Please try later again."
    }
}