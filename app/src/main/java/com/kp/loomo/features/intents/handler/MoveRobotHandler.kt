package com.kp.loomo.features.intents.handler

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler
import com.kp.loomo.features.robot.RobotManager
import javax.inject.Inject

private val TAG = "MoveRobotHandler"

class MoveRobotHandler constructor(private var robotManager: RobotManager) : IntentMessageHandler {

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        return intentMessage.queryResult.intent.displayName == "moveRobot"
    }

    override fun handle(intentMessage: DetectIntentResponse): String {

        val direction = intentMessage.queryResult.parameters.fieldsMap["direction"]!!.stringValue
        robotManager.drive(direction)

        return "Moving $direction"
    }

    override fun canHandleOffline(intentMessage: String): Boolean {
        return intentMessage.startsWith("drive")
    }

    override fun handleOffline(intentMessage: String): String {
        when {
            intentMessage.startsWith("drive") -> {
                val direction = intentMessage.substring(intentMessage.lastIndexOf(" ")+1)

                // TODO call drive in robotManager

                return "Driving $direction"
            }
        }
        return "I want to drive, but don't know where. Please try again"
    }
}