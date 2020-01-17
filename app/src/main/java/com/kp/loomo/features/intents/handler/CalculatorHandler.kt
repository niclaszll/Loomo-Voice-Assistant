package com.kp.loomo.features.intents.handler

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler

private val TAG = "CalculatorHandler"

class CalculatorHandler : IntentMessageHandler {

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        return intentMessage.queryResult.intent.displayName == "calculate"
    }

    /**
     * if in fun to decide which operation is asked
     *
     */
    override fun handle(intentMessage: DetectIntentResponse): String {

        val firstNumber = intentMessage.queryResult.parameters.fieldsMap["number1"]!!.numberValue
        val secondNumber = intentMessage.queryResult.parameters.fieldsMap["number2"]!!.numberValue

        val sum = firstNumber + secondNumber

        return "${sum.toInt()}, right?"
    }

    override fun canHandleOffline(intentMessage: String): Boolean {
        return false
    }

    override fun handleOffline(intentMessage: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}