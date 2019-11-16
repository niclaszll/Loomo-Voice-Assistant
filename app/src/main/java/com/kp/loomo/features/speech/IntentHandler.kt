package com.kp.loomo.features.speech

import ai.snips.hermes.IntentMessage
import android.util.Log
import com.kp.loomo.features.speech.handler.CalculatorHandler
import com.kp.loomo.features.speech.handler.WeatherHandler

object IntentHandler {
    private val listOfHandler = arrayListOf<IntentMessageHandler>()

    init {
        listOfHandler.add(WeatherHandler())
        listOfHandler.add(CalculatorHandler())
    }

    fun handleIntent(intentMessage: IntentMessage): String {

        Log.d("test", intentMessage.toString())
        listOfHandler.forEach {
            if (it.canHandle(intentMessage)) {
                return it.handle(intentMessage)
            }
        }
        return "I understood '${intentMessage.input}'. Unfortunately I don't know what to do. :("
    }
}