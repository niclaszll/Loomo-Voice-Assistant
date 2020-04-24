package com.kp.loomo.features.intents.handler

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.R
import com.kp.loomo.features.intents.IntentMessageHandler
import com.kp.loomo.features.robot.MediaManager

private const val TAG = "StopMusicHandler"

class StopMusicHandler(private val mediaManager: MediaManager) : IntentMessageHandler {

    private val keywords = arrayOf(
        "stop music", "no music", "stop the song",
        "don't play music", "don not play music", "stop"
    )

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        return intentMessage.queryResult.intent.displayName == "StopMusic"
    }

    override fun handle(intentMessage: DetectIntentResponse): String {
        mediaManager.resetPlayer()
        return "Ok."
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
        mediaManager.resetPlayer()
        return "Stopped music."
    }

}