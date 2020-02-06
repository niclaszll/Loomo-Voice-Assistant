package com.kp.loomo.features.intents.handler

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler
import com.kp.loomo.features.robot.TimerManager

private val TAG = "TimerHandler"

class TimerHandler constructor(private var timerManager: TimerManager) : IntentMessageHandler {

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        return intentMessage.queryResult.intent.displayName == "Timer"
    }

    override fun handle(intentMessage: DetectIntentResponse): String {

        val seconds = intentMessage.queryResult.parameters.fieldsMap["seconds"]!!.stringValue
        val minutes = intentMessage.queryResult.parameters.fieldsMap["minutes"]!!.stringValue

        if(seconds != "" && minutes != "") {
            timerManager.setTimer(seconds.toInt(), minutes.toInt())
            return "Setting timer for $minutes minutes and $seconds seconds."
        } else if (seconds != "") {
            timerManager.setTimer(seconds.toInt(), 0)
            return "Setting timer for $seconds seconds."
        } else if (minutes != "") {
            timerManager.setTimer(0, minutes.toInt())
            return "Setting timer for $minutes minutes."
        }

        return "Error creating a timer."
    }

    override fun canHandleOffline(intentMessage: String): Boolean {
        return false
    }

    override fun handleOffline(intentMessage: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}