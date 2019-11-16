package com.kp.loomo.features.speech.handler

import ai.snips.hermes.IntentMessage
import com.kp.loomo.features.speech.IntentMessageHandler

class WeatherHandler : IntentMessageHandler {

    override fun canHandle(intentMessage: IntentMessage): Boolean {
        return intentMessage.intent.intentName == "niclaszll:AskWeather"
    }

    override fun handle(intentMessage: IntentMessage): String {

        val weatherAnswers = arrayOf("The weather is nice", "The weather is quite good", "It's raining later")

        return weatherAnswers[(0..2).random()]
    }
}