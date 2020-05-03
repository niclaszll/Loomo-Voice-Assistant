package com.kp.loomo.features.intents.handler

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler

private const val TAG = "CalculatorHandler"

class CalculatorHandler : IntentMessageHandler {

    private val keywords = arrayOf(
        "plus", "sum", "add", "addition",
        "minus", "difference", "subtraction", "subtract",
        "multiplication", "times", "product",
        "quotient", "division", "divided by"
    )

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        return intentMessage.queryResult.intent.displayName == "calculate"
    }

    override fun handle(intentMessage: DetectIntentResponse): String {

        val firstNumber = intentMessage.queryResult.parameters.fieldsMap["number1"]!!.numberValue
        val secondNumber = intentMessage.queryResult.parameters.fieldsMap["number2"]!!.numberValue

        when (intentMessage.queryResult.parameters.fieldsMap["operation"]!!.stringValue) {
            "Addition" -> {
                val sum = firstNumber + secondNumber
                return "${sum.toInt()}, right?"
            }
            "Subtraction" -> {
                val sub = firstNumber - secondNumber
                return "${sub.toInt()}, right?"
            }
            "Multiplication" -> {
                val mul = firstNumber * secondNumber
                return "${mul.toInt()}, right?"
            }
            "Quotient" -> {
                val div = firstNumber / secondNumber
                return "${div.toInt()}, right?"
            }
            else -> {
                return "Not a valid operation."
            }
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

        for (keyword in keywords) {
            if ((keyword == "plus" || keyword == "sum" || keyword == "add" || keyword == "addition") && intentMessage.contains(
                    keyword,
                    true
                )
            ) {
                val numberArray = intentMessage.split(" ", ignoreCase = true)
                val sum =
                    mapStringToNumber(numberArray[0]) + mapStringToNumber(numberArray[numberArray.lastIndex])
                return "It's $sum, right?"
            }

            if ((keyword == "minus" || keyword == "difference" || keyword == "subtraction" || keyword == "subtract") && intentMessage.contains(
                    keyword,
                    true
                )
            ) {
                val numberArray = intentMessage.split(" ", ignoreCase = true)
                val dif =
                    mapStringToNumber(numberArray[0]) - mapStringToNumber(numberArray[numberArray.lastIndex])
                return "It's $dif, right?"
            }

            if ((keyword == "multiplication" || keyword == "times" || keyword == " product") && intentMessage.contains(
                    keyword,
                    true
                )
            ) {
                val numberArray = intentMessage.split(" ", ignoreCase = true)
                val prod =
                    mapStringToNumber(numberArray[0]) * mapStringToNumber(numberArray[numberArray.lastIndex])
                return "It's $prod, right?"
            }

            if ((keyword == "quotient" || keyword == "division" || keyword == "divided by") && intentMessage.contains(
                    keyword,
                    true
                )
            ) {
                val numberArray = intentMessage.split(" ", ignoreCase = true)
                val quo =
                    mapStringToNumber(numberArray[0]) / mapStringToNumber(numberArray[numberArray.lastIndex])
                return "It's $quo, right?"
            }
        }

        return ""
    }

    private fun mapStringToNumber(stringNumber: String): Int {
        when (stringNumber) {
            "zero" -> return 0
            "one" -> return 1
            "two" -> return 2
            "three" -> return 3
            "four" -> return 4
            "five" -> return 5
            "six" -> return 6
            "seven" -> return 7
            "eight" -> return 8
            "nine" -> return 9
            "ten" -> return 10
        }
        return 0
    }
}
