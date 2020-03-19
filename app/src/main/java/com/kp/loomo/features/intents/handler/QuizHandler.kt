package com.kp.loomo.features.intents.handler

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler

private val TAG = "QuizHandler"

class QuizHandler : IntentMessageHandler {

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
        return "$question Your answer?"
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
        return "There is no internet connection at the present time. Please try later again."
    }
}