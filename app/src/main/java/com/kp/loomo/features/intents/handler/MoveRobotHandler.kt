package com.kp.loomo.features.intents.handler

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler
import com.kp.loomo.features.robot.RobotManager
import javax.inject.Inject

private val TAG = "MoveRobotHandler"

class MoveRobotHandler constructor(private var robotManager: RobotManager) : IntentMessageHandler {

    // possible commands, extend here and in intent grammar
    private val commands = arrayOf("drive", "go", "move")

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        return intentMessage.queryResult.intent.displayName == "moveRobot"
    }

    override fun handle(intentMessage: DetectIntentResponse): String {

        val direction = intentMessage.queryResult.parameters.fieldsMap["direction"]!!.stringValue
        robotManager.drive(direction)

        return "Driving $direction"
    }

    override fun canHandleOffline(intentMessage: String): Boolean {

        for (cmd in commands) {
            if (intentMessage.startsWith(cmd)) return true
        }
        return false
    }

    override fun handleOffline(intentMessage: String): String {

        for (cmd in commands) {
            if (intentMessage.startsWith(cmd)) {
                val direction = intentMessage.substringAfter(cmd).trim()
                var parsedDirection = ""

                // map direction
                when (direction) {
                    "right" -> parsedDirection = "right"
                    "left" -> parsedDirection = "left"
                    "backward", "backwards", "back" -> parsedDirection = "backward"
                    "forward" -> parsedDirection = "forward"
                }

                robotManager.drive(parsedDirection)

                return "Driving $parsedDirection"
            }
        }
        return "I want to drive, but don't know where. Please try again"
    }
}