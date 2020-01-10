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

        val cmd = intentMessage.queryResult.parameters.fieldsMap["FollowCommand"]!!.stringValue

        if (cmd == "Start") {
            robotManager.actionInitiateTrack()
            return "Following"
        } else if (cmd == "Stop"){
            robotManager.actionTerminateTrack()
            return "I'm no longer following"
        }

        return "I didn't understand if I should really follow you"
    }

    override fun canHandleOffline(intentMessage: String): Boolean {
        return false
    }

    override fun handleOffline(intentMessage: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}