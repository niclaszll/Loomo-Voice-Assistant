package com.kp.loomo.features.intents.handler

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler
import com.kp.loomo.features.robot.RobotManager
import javax.inject.Inject

private val TAG = "FollowRobotHandler"

class FollowRobotHandler constructor(private var robotManager: RobotManager) : IntentMessageHandler {

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        return intentMessage.queryResult.intent.displayName == "Follow"
    }

    override fun handle(intentMessage: DetectIntentResponse): String {

        val cmd = intentMessage.queryResult.parameters.fieldsMap["CommandFollow"]!!.stringValue
        robotManager.actionInitiateTrack()

        return "Following"
    }
}