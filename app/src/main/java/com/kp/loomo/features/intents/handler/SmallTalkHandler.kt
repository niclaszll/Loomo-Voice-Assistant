package com.kp.loomo.features.intents.handler

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler

private val TAG = "SmallTalkHandler"

class SmallTalkHandler : IntentMessageHandler {

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        return intentMessage.queryResult.intent.displayName == "smallTalk"
    }

    override fun handle(intentMessage: DetectIntentResponse): String {
        val date = intentMessage.queryResult.parameters.fieldsMap["date"]!!.numberValue
        val time = intentMessage.queryResult.parameters.fieldsMap["time"]!!.numberValue
        val musicArtist = intentMessage.queryResult.parameters.fieldsMap["music-artist"]!!.numberValue
        val temperature = intentMessage.queryResult.parameters.fieldsMap["temperature"]!!.numberValue
        val city = intentMessage.queryResult.parameters.fieldsMap["geo-city"]!!.numberValue
        val name = intentMessage.queryResult.parameters.fieldsMap["Robotername"]!!.numberValue
        val message = intentMessage.queryResult.fulfillmentText
        return "${message}"
    }
}