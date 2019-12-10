package com.kp.loomo.features.intents

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.handler.CalculatorHandler

/**
 * Intent handler that decides which specific handler should continue
 */
object IntentHandler {
    private val listOfHandler = arrayListOf<IntentMessageHandler>()

    // add all handlers here
    init {
        listOfHandler.add(CalculatorHandler())
    }

    fun handleIntent(intentMessage: DetectIntentResponse): String {

        // iterate over each handler and check if handler can handler intentMessage
        listOfHandler.forEach {
            if (it.canHandle(intentMessage)) {
                return it.handle(intentMessage)
            }
        }
        return "I understood '${intentMessage.queryResult.queryText}'. Unfortunately I don't know what to do. :("
    }
}