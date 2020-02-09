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

        if (cmd == "Reset head") {
            robotManager.resetHead()
            return "Resetting head"
        }

        //TODO implement look left/right

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
                    //TODO implement look right
                } else if (intentMessage.contains("left", true)) {
                    direction = "left"
                    //TODO implement look left
                }
                return "Looking $direction."
            }
        }
        return "Sorry, I can't do that."
    }
}