package com.kp.loomo.features.intents.handler

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler
import com.kp.loomo.features.robot.RobotManager
import javax.inject.Inject

private val TAG = "FollowRobotHandler"

class FollowRobotHandler constructor(private var robotManager: RobotManager) :
    IntentMessageHandler {

    // possible keywords, extend here and in intent grammar
    private val keywords = arrayOf("follow", "following")

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        return intentMessage.queryResult.intent.displayName == "Follow"
    }

    override fun handle(intentMessage: DetectIntentResponse): String {

        val cmd = intentMessage.queryResult.parameters.fieldsMap["FollowCommand"]!!.stringValue

        if (cmd == "Start following") {
            robotManager.actionInitiateTrack()
            return "Following"
        } else if (cmd == "Stop following") {
            robotManager.actionTerminateTrack()
            return "I'm no longer following"
        }

        return "I didn't understand if I should really follow you"
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
        var follow = false

        when (intentMessage) {
            "start following me", "start following", "follow", "follow me", "start follow" -> follow =
                true
        }

        return if (follow) {
            robotManager.actionInitiateTrack()
            "Following"
        } else {
            robotManager.actionTerminateTrack()
            "I'm no longer following"
        }
    }
}