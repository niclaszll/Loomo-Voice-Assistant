package com.kp.loomo.features.intents.handler

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler
import com.kp.loomo.features.robot.SystemSettingsManager

private val TAG = "FollowRobotHandler"

class SystemHandler constructor(private var systemSettingsManager: SystemSettingsManager) : IntentMessageHandler {

    // possible keywords, extend here and in intent grammar
    private val keywords = arrayOf("brightness", "volume", "mute")
    private val numbers = arrayOf("zero", "ten", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety", "one hundred")

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        return intentMessage.queryResult.intent.displayName == "SystemSettings"
    }

    override fun handle(intentMessage: DetectIntentResponse): String {

        val cmd = intentMessage.queryResult.parameters.fieldsMap["SystemSettingsCmd"]!!.stringValue

        val rawValue = intentMessage.queryResult.parameters.fieldsMap["percentage"]!!.stringValue
        var value = 0
        if (rawValue != "") {
            value = intentMessage.queryResult.parameters.fieldsMap["percentage"]!!.stringValue.trim().trim("%".single()).toInt()
            if (value !in 0..100) value = 50
        }

        when (cmd) {
            "brightness" -> systemSettingsManager.setBrightness(value)
            "volume" -> systemSettingsManager.setAudioVolume(value)
            "mute" -> {
                systemSettingsManager.setAudioVolume(0)
                return "Set volume to 0"
            }
        }

        return "Setting $cmd to $value%"
    }

    override fun canHandleOffline(intentMessage: String): Boolean {

        for (keyword in keywords) {
            if (intentMessage.contains(keyword,  true)) {
                return true
            }
        }
        return false
    }

    override fun handleOffline(intentMessage: String): String {

        for (keyword in keywords) {
            if (keyword == "brightness" && intentMessage.contains(keyword,  true)) {
                for (number in numbers) {
                    if (intentMessage.contains(number,  true)) {
                        val value = mapStringToNumber(number)
                        systemSettingsManager.setBrightness(value)
                        return "Setting brightness to $value%"
                    }
                }
                return "Sorry I didn't get the volume value."
            } else if (keyword == "volume" && intentMessage.contains(keyword,  true)) {
                for (number in numbers) {
                    if (intentMessage.contains(number,  true)) {
                        val value = mapStringToNumber(number)
                        systemSettingsManager.setAudioVolume(value)
                        return "Setting volume to $value%"
                    }
                }
                return "Sorry I didn't get the volume value."
            } else if (keyword == "mute" && intentMessage.contains(keyword,  true)) {
                systemSettingsManager.setAudioVolume(0)
                return "Setting volume to 0%"
            }
        }
        return "Sorry there was an error with adjusting the settings."
    }

    private fun mapStringToNumber(stringNumber: String): Int {
        when (stringNumber) {
            "zero" -> return 0
            "ten" -> return 10
            "twenty" -> return 20
            "thirty" -> return 30
            "forty" -> return 40
            "fifty" -> return 50
            "sixty" -> return 60
            "seventy" -> return 70
            "eighty" -> return 80
            "ninety" -> return 90
            "one hundred" -> return 100
        }
        return 50
    }
}