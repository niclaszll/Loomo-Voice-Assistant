package com.kp.loomo.features.speech

import ai.snips.hermes.IntentMessage
import android.util.Log
import com.kp.loomo.features.speech.handler.CalculatorHandler
import com.kp.loomo.features.speech.handler.WeatherHandler

/**
 * Intent handler that decides which specific handler should continue
 */
object IntentHandler {
    private val listOfHandler = arrayListOf<IntentMessageHandler>()

    // add all handlers here
    init {
        listOfHandler.add(WeatherHandler())
        listOfHandler.add(CalculatorHandler())
    }

    fun handleIntent(intentMessage: IntentMessage): String {

        // iterate over each handler and check if handler can handler intentMessage
        listOfHandler.forEach {
            if (it.canHandle(intentMessage)) {
                return it.handle(intentMessage)
            }
        }
        return "I understood '${intentMessage.input}'. Unfortunately I don't know what to do. :("
    }
}