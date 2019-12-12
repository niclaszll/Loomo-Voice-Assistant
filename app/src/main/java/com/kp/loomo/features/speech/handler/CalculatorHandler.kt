package com.kp.loomo.features.speech.handler

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.speech.IntentMessageHandler

class CalculatorHandler : IntentMessageHandler {

    private val tag = "CalculatorHandler"

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        return intentMessage.queryResult.intent.displayName == "calculate"
    }

    /**
     * if in fun to decide which operation is asked
     *
     */
    override fun handle(intentMessage: DetectIntentResponse): String {

        var sum = 0.0

        val firstNumber = intentMessage.queryResult.parameters.fieldsMap["number1"]!!.numberValue
        val secondNumber = intentMessage.queryResult.parameters.fieldsMap["number2"]!!.numberValue

        sum = firstNumber + secondNumber


        return "${sum.toInt()}, right?"
    }
}