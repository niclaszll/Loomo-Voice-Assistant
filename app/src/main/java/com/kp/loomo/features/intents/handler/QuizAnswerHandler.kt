package com.kp.loomo.features.intents.handler

import android.media.MediaPlayer
import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler
import java.util.concurrent.ThreadLocalRandom

private const val TAG = "QuizAnswerHandler"

class QuizAnswerHandlerHandler : IntentMessageHandler {

    private val mp = MediaPlayer()


    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        return intentMessage.queryResult.intent.displayName == "Quiz.answer"
    }

    override fun handle(intentMessage: DetectIntentResponse): String {

        val solve = intentMessage.queryResult.fulfillmentText
        if (solve.startsWith("C")) {
            mp.reset()
            mp.setDataSource("https://actions.google.com/sounds/v1/cartoon/wood_plank_flicks.ogg")
            mp.prepare()
            mp.start()
            Thread.sleep(1000)
        } else {
            mp.reset()
            mp.setDataSource("https://actions.google.com/sounds/v1/cartoon/slide_whistle_to_drum.ogg")
            mp.prepare()
            mp.start()
            Thread.sleep(3000)
        }
        return "$solve"
    }


    override fun canHandleOffline(intentMessage: String): Boolean {
        return false
    }

    override fun handleOffline(intentMessage: String): String {
        return ""
    }

}