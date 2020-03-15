package com.kp.loomo.features.intents.handler

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler

private const val TAG = "QuizHandler"

class QuizHandler : IntentMessageHandler {

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        return intentMessage.queryResult.intent.displayName == "Quiz"
    }

    override fun handle(intentMessage: DetectIntentResponse): String {

        val answer = intentMessage.queryResult.parameters.fieldsMap["number1"]!!.stringValue

        return "$answer, right?"
    }

    override fun canHandleOffline(intentMessage: String): Boolean {
        return false
    }

    override fun handleOffline(intentMessage: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}