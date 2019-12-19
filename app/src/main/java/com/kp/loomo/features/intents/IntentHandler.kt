package com.kp.loomo.features.intents

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
        listOfHandler.add(CalendarHandler())
        listOfHandler.add(SmallTalkHandler())
    }

    fun handleIntent(intentMessage: DetectIntentResponse): String {

        // iterate over each handler and check if handler can handler intentMessage
        listOfHandler.forEach {
            if (it.canHandle(intentMessage)) {
                return it.handle(intentMessage)
            }
        }
        return "I understood '${intentMessage.queryResult.queryText}'. Unfortunately I don't know what to do. :("
    }
}