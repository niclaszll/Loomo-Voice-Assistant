package com.kp.loomo.features.intents.handler

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.R
import com.kp.loomo.features.intents.IntentMessageHandler
import com.kp.loomo.features.robot.MediaManager

private const val TAG = "MusicHandler"

class MusicHandler(private val mediaManager: MediaManager) : IntentMessageHandler {

    private val keywords = arrayOf(
        "music", "song", "play a song",
        "play music", "play some music", "a song"
    )


    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        return intentMessage.queryResult.intent.displayName == "Music"
    }

    override fun handle(intentMessage: DetectIntentResponse): String {
        handlePlayCmd()
        return ""
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
        handlePlayCmd()
        return ""
    }

    private fun handlePlayCmd(){
        mediaManager.playLocalSound(R.raw.sample)
    }

}