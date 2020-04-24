package com.kp.loomo.features.intents.handler

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler
import com.kp.loomo.features.robot.MediaManager

private const val TAG = "QuizAnswerHandler"

class QuizAnswerHandler(private val mediaManager: MediaManager) : IntentMessageHandler {

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        return intentMessage.queryResult.intent.displayName == "Quiz.answer"
    }

    override fun handle(intentMessage: DetectIntentResponse): String {

        val solve = intentMessage.queryResult.fulfillmentText
        if (solve.startsWith("Con")) {
            mediaManager.playSoundFromUrl("https://actions.google.com/sounds/v1/cartoon/wood_plank_flicks.ogg")
            Thread.sleep(1000)
        } else {
            mediaManager.playSoundFromUrl("https://actions.google.com/sounds/v1/cartoon/slide_whistle_to_drum.ogg")
            Thread.sleep(3000)
        }
        mediaManager.resetPlayer()
        return "$solve"
    }

    override fun canHandleOffline(intentMessage: String): Boolean {
        return false
    }

    override fun handleOffline(intentMessage: String): String {
        return ""
    }

}