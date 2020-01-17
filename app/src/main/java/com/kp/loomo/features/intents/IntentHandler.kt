package com.kp.loomo.features.intents

import android.util.Log
import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.handler.*
import com.kp.loomo.features.robot.RobotManager
import javax.inject.Inject

/**
 * Intent handler that decides which specific handler should continue
 */
class IntentHandler @Inject constructor(private var robotManager: RobotManager) {
    private val listOfHandler = arrayListOf<IntentMessageHandler>()

    // add all handlers here
    init {
        listOfHandler.add(CalculatorHandler())
        listOfHandler.add(MoveRobotHandler(robotManager))
        listOfHandler.add(FollowRobotHandler(robotManager))
        listOfHandler.add(GeneralRobotHandler(robotManager))
        listOfHandler.add(CalendarHandler())
    }

    /**
     * Check for online intent handlers and handle if possible
     */
    fun handleIntent(intentMessage: DetectIntentResponse): String {

        Log.d("IntentHandler", intentMessage.toString())

        // iterate over each handler and check if handler can handle intentMessage
        listOfHandler.forEach {
            if (it.canHandle(intentMessage)) {
                return it.handle(intentMessage)
            }
        }
        return intentMessage.queryResult.fulfillmentText
        // return "I understood '${intentMessage.queryResult.queryText}'. Unfortunately I don't know what to do. :("
    }

    /**
     * Check for offline intent handlers and handle if possible
     */
    fun handleOfflineIntent(intentMessage: String): String {

        // iterate over each handler and check if handler can handle response
        listOfHandler.forEach {
            if (it.canHandleOffline(intentMessage)) {
                return it.handleOffline(intentMessage)
            }
        }
        return "I understood '${intentMessage}'. Unfortunately I don't know what to do. :("
    }
}