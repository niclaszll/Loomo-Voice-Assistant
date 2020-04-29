package com.kp.loomo.features.intents.handler

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler

private const val TAG = "CalculatorHandler"

class CalculatorHandler : IntentMessageHandler {
   
    private val keywords = arrayOf(
	"plus", "sum", "add", "addition",
	"minus", "difference", "substraction", "subtract",
	"multiplication", "times", "product",
	"quotient", "division", "divided by"
    )

    private val firstNumbers = arrayOf(
	"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"
    )

    private val secondNumbers = arrayOf(
	"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"
    )

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        return intentMessage.queryResult.intent.displayName == "calculate"
    }

   override fun handle(intentMessage: DetectIntentResponse): String {

        val firstNumber = intentMessage.queryResult.parameters.fieldsMap["number1"]!!.numberValue
        val secondNumber = intentMessage.queryResult.parameters.fieldsMap["number2"]!!.numberValue

		val operation = intentMessage.queryResult.parameters.fieldsMap["operation"]!!.stringValue
		if (operation == "Addition") {
			val sum = firstNumber + secondNumber
			return "${sum.toInt()}, right?"
		} else if (operation == "Subtraction") {
			val sub = firstNumber - secondNumber
			return "${sub.toInt()}, right?"
		} else if (operation == "Multiplication") {
			val mul = firstNumber * secondNumber
			return "${mul.toInt()}, right?"
		} else if (operation == "Quotient") {
			val div = firstNumber / secondNumber
			return "${div.toInt()}, right?"
		} else {
			return "Not a valid operation."
		}   
    }

	

    override fun canHandleOffline (intentMessage: String): Boolean{
	for (keywords in keyword){
		if (intentMessage.contains(keyword, true) {
			return true 
		}
	}
	return false
    }

    override fun handleOffline (intentMessage: String): String {
	for (keyword in keywords) {
		if (keyword == "plus" || keyword == "sum" || keyword == "add" || keyword == "addition") {
			if (intentMessage.contains(keyword, true) {
				for (firstNumber in firstNumbers)
					if (intentMessage.contains(firstNumber, true) {
						val firstValue = mapStringToNumber(firstNumber)
						for (secondNumber in secondNumbers) {
							if (intentMessage.contains(secondNumber, true) {
								val secondValue = mapStringToNumber(secondNumber)
								val result = firstValue + secondValue
								return "It's $result, right?"
							}
						}
						return "Sorry, I didn't get the second number."
					}
				}
				return "'Sorry, I didn't get that."
			}
		}
		if (keyword == "minus" || keyword == "difference" || keyword == "substraction" || keyword == "subtract") {
			if (intentMessage.contains(keyword, true) {
				for (firstNumber in firstNumbers) {
					if (intentMessage.contains(firstNumber, true)
						val firstValue = mapStringToNumber(firstNumber)
						for (secondNumber in secondNumbers) {
							if (intentMessage.contains(secondNumber, true) {
								val secondValue = mapStringToNumber(secondNumber)
								val result = firstValue - secondValue
								return "It's $result, right?"
							}
						}
						return "Sorry, I didn't get the second number."
					}
				}
				return "Sorry, I din't get that."
			}		
		}
		if (keyword == "multiplication" || keyword == "times" || keyword == " product") {
			if (intentMessage.contains(keyword, true) {
				for (firstNumber in firstNumbers) {
					if (intentMessage.contains(firstNumber, true) {
						val firstValue = mapStringToNumber(firstNumber)
						for(secondNumber in secondNumbers) {
							if (intentMessage.contains(secondNumber, true) {
								val secondValue = mapStringToNumber(secondNumber)
								val result = firstValue * secondValue
								return "It's $result, right?"
							}
						}
						return "Sorry, I didn't get the second number."
					}
				}
				return "Sorry, I didn't get that."
			}
		}
		if (keyword == "quotient" || keyword == "division" || keyword == "divided by") {
			if (intentMessage.contains(keyword, true) {
				for (firstNumber in firstNumbers) {
					if (intentMessage.contains(firstNumber, true) {
						val firstValue = mapStringToNumber(firstNumber)
						for (secondNumber in secondNumbers) {
							if (intentMessage.contains(secondNumber, true) {
								val secondValue = mapStringToNumber(secondNumber)
								val result = firstValue / secondValue
								return "It's $result, right?"
							}
						}
						return "Sorry, I didn't get the second number."
					}
				}
				return "Sorry, I didn't get that."
			}
		}
	}
    }

    private fun mapStringToNumber (stringNumber: String): Int {
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
    }	
}
