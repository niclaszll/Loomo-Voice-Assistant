package com.kp.loomo.features.intents.handler

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler

private val TAG = "CalculatorHandler"

class CalculatorHandler : IntentMessageHandler {

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        return intentMessage.queryResult.intent.displayName == "calculate"
    }

   override fun handle(intentMessage: DetectIntentResponse): String {

        val firstNumber = intentMessage.queryResult.parameters.fieldsMap["number1"]!!.numberValue
        val secondNumber = intentMessage.queryResult.parameters.fieldsMap["number2"]!!.numberValue

		val operation = intentMessage.queryResult.parameters.fieldsMap["operation"]!!.stringValue
		if(operation == "Addition){
			val sum = firstNumber + secondNumber
			return "${sum.toInt()}, right?"
		}else if(operation == "Subtraction"){
			val sub = firstNumber - secondNumber
			return "${sub.toInt()}, right?"
		}else if(operation == "Multiplication"){
			val mul = firstNumber * secondNumber
			return "${mul.toInt()}, right?"
		}else if(operation == "Quotient"){
			val div = firstNumber / secondNumber
			return "${div.toInt()}, right?"
		}else{
			return "Not a valid operation."
		}   
    }

    override fun canHandleOffline(intentMessage: String): Boolean {
        return false
    }

    override fun handleOffline(intentMessage: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
