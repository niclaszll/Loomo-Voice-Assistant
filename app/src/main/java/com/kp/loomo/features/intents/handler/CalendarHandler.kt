package com.kp.loomo.features.intents.handler

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler

class CalendarHandler : IntentMessageHandler {

    private val tag = "CalendarHandler"

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        return intentMessage.queryResult.intent.displayName == "Calendar"
    }
    override fun handle(intentMessage: DetectIntentResponse): String {
        val date1 = intentMessage.queryResult.parameters.fieldsMap["date"]!!.numberValue
        val event = intentMessage.queryResult.parameters.fieldsMap["event"]!!.stringValue
        val time1 = intentMessage.queryResult.parameters.fieldsMap["time"]!!.numberValue
        val message = intentMessage.queryResult.fulfillmentText
        return "${message}"
    }
}