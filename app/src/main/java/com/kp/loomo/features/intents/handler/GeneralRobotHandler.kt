package com.kp.loomo.features.intents.handler

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler
import com.kp.loomo.features.robot.RobotManager
import javax.inject.Inject

private val TAG = "GeneralRobotHandler"

class GeneralRobotHandler constructor(private var robotManager: RobotManager) :
    IntentMessageHandler {

    private val keywords = arrayOf("reset", "look")

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        return intentMessage.queryResult.intent.displayName == "GeneralRobot"
    }

    override fun handle(intentMessage: DetectIntentResponse): String {

        val cmd = intentMessage.queryResult.parameters.fieldsMap["Command"]!!.stringValue
        val direction = intentMessage.queryResult.parameters.fieldsMap["Direction"]!!.stringValue

        if (cmd == "Reset head") {
            robotManager.resetHead()
            return "Resetting head"
        } else if (cmd == "Look") {
            if (direction == "left") {
                robotManager.lookDirection("left")
            } else if (direction == "right") {
                robotManager.lookDirection("right")
            }
            return "Looking $direction"
        }

        return "I didn't understand what to do"
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
            if (keyword == "reset" && intentMessage.contains(keyword, true)) {
                robotManager.resetHead()
                return "Resetting head"
            } else if (keyword == "look" && intentMessage.contains(keyword, true)) {
                var direction = "left"
                if (intentMessage.contains("right", true)) {
                    direction = "right"
                    robotManager.lookDirection("right")
                } else if (intentMessage.contains("left", true)) {
                    direction = "left"
                    robotManager.lookDirection("left")
                }
                return "Looking $direction."
            }
        }
        return "Sorry, I can't do that."
    }
}