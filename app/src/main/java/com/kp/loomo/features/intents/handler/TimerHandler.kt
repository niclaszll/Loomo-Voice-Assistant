package com.kp.loomo.features.intents.handler

import android.util.Log
import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler
import com.kp.loomo.features.robot.TimerManager

private const val TAG = "TimerHandler"

class TimerHandler constructor(private var timerManager: TimerManager) : IntentMessageHandler {

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        return intentMessage.queryResult.intent.displayName == "Timer" || intentMessage.queryResult.intent.displayName == "TimerCommand"
    }

    override fun handle(intentMessage: DetectIntentResponse): String {

        if(intentMessage.queryResult.intent.displayName == "Timer") {
            if (timerManager.activeTimer != null) {
                timerManager.activeTimer!!.cancel()
                Log.d(TAG, "Current timer cancelled.")
            }

            val seconds = intentMessage.queryResult.parameters.fieldsMap["seconds"]!!.numberValue
            val minutes = intentMessage.queryResult.parameters.fieldsMap["minutes"]!!.numberValue

            if(seconds != 0.0 && minutes != 0.0) {
                timerManager.setTimer(seconds.toInt(), minutes.toInt())
                return "Setting timer for ${minutes.toInt()} minutes and ${seconds.toInt()} seconds."
            } else if (seconds != 0.0) {
                timerManager.setTimer(seconds.toInt(), 0)
                return "Setting timer for ${seconds.toInt()} seconds."
            } else if (minutes != 0.0) {
                timerManager.setTimer(0, minutes.toInt())
                return "Setting timer for ${minutes.toInt()} minutes."
            }

            return "Error creating a timer."
        } else if(intentMessage.queryResult.intent.displayName == "TimerCommand") {

            when (intentMessage.queryResult.parameters.fieldsMap["timerCommand"]!!.stringValue) {
                "stop" -> if (timerManager.activeTimer != null) {
                    timerManager.activeTimer!!.cancel()
                    return "Current timer canceled."
                }
                "pause" -> if (timerManager.activeTimer != null) {
                    timerManager.paused = true
                    timerManager.activeTimer!!.cancel()
                    return "Current timer paused."
                }
                "resume" -> if (timerManager.paused) {
                    timerManager.setTimer(timerManager.remainingTime / 1000,0)
                    return "Resuming timer with ${timerManager.remainingTime / 1000} seconds remaining."
                }
            }

            return "Sorry, I can't do that."
        }

        return "Timer error."
    }

    override fun canHandleOffline(intentMessage: String): Boolean {
        return false
    }

    override fun handleOffline(intentMessage: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}