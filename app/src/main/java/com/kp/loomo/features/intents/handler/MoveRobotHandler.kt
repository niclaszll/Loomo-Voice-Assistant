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
}