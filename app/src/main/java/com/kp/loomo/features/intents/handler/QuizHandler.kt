package com.kp.loomo.features.intents.handler

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.R
import com.kp.loomo.features.intents.IntentMessageHandler
import com.kp.loomo.features.robot.MediaManager
import java.util.concurrent.ThreadLocalRandom

private const val TAG = "QuizHandler"

class QuizHandler(private val mediaManager: MediaManager) : IntentMessageHandler {

    private val keywords = arrayOf(
        "quiz", "question", "questions",
        "riddle", "task", "cognitive",
        "brain", "training", "memory",
        "cognition", "mental",
        "teaser", "teasers", "jogging"
    )

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        return intentMessage.queryResult.intent.displayName == "Quiz"
    }

    override fun handle(intentMessage: DetectIntentResponse): String {

        val question = intentMessage.queryResult.fulfillmentText.toString()
        return if (question.isNullOrBlank()) {
            // sometimes cannot read db content (at the beginning), so don't ask for answer
            // reset context 'question' in Dialogflow,
            // therefore no question context in intent but in fulfillment
            "Error while receiving a question for you. Please try again later!"
        } else {
            mediaManager.playSoundFromUrl("https://actions.google.com/sounds/v1/cartoon/metal_twang.ogg")
            Thread.sleep(1000)
            question
        }
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
        val rI0 = ThreadLocalRandom.current().nextInt(1, 1000)
        val rI1 = ThreadLocalRandom.current().nextInt(1, 1000)

        return "You are offline. So I just have a task for you. Calculate $rI0 + $rI1?"
    }
}