package com.kp.loomo.features.intents.handler

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler

class CalendarHandler : IntentMessageHandler {

    private val tag = "CalendarHandler"

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        // return intentMessage.queryResult.intent.displayName == "Calendar"
        return false
    }
    override fun handle(intentMessage: DetectIntentResponse): String {
        return ""
    }

    override fun canHandleOffline(intentMessage: String): Boolean {
        return false
    }

    override fun handleOffline(intentMessage: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}