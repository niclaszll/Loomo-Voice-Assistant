package com.kp.loomo.features.intents.handler

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler
import java.text.DateFormat
import java.util.*

private const val TAG = "DateTimeHandler"

class DateTimeHandler : IntentMessageHandler {

    private val keywords = arrayOf("time", "how late")

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        return intentMessage.queryResult.intent.displayName == "Time"
    }

    override fun handle(intentMessage: DetectIntentResponse): String {

        val currentTime: Date = Calendar.getInstance().time
        val formatter: DateFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US)

        return "The current time is ${formatter.format(currentTime)}"
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

        val currentTime: Date = Calendar.getInstance().time
        val formatter: DateFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US)

        return "The current time is ${formatter.format(currentTime)}"
    }
}