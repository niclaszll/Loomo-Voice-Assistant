package com.kp.loomo.features.intents

import android.net.ConnectivityManager
import android.content.SharedPreferences
import android.util.Log
import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.handler.*
import com.kp.loomo.features.robot.MediaManager
import com.kp.loomo.features.robot.RobotManager
import com.kp.loomo.features.robot.SystemSettingsManager
import com.kp.loomo.features.robot.TimerManager
import javax.inject.Inject

/**
 * Intent handler that decides which specific handler should continue
 */
class IntentHandler @Inject constructor(
    robotManager: RobotManager,
    systemSettingsManager: SystemSettingsManager,
    timeManager: TimerManager,
    connectivityManager: ConnectivityManager,
    sharedPreferences: SharedPreferences,
    mediaManager: MediaManager
) {
    private val listOfHandler = arrayListOf<IntentMessageHandler>()

    /**
     * Initialize list of all custom handlers
     */
    init {
        listOfHandler.add(CalculatorHandler())
        listOfHandler.add(MoveRobotHandler(robotManager))
        listOfHandler.add(FollowRobotHandler(robotManager))
        listOfHandler.add(GeneralRobotHandler(robotManager))
        listOfHandler.add(CalendarHandler(sharedPreferences))
        listOfHandler.add(SystemSettingsHandler(systemSettingsManager))
        listOfHandler.add(TimerHandler(timeManager))
        listOfHandler.add(DateTimeHandler())
        listOfHandler.add(OnlineTestHandler(connectivityManager))
        listOfHandler.add(WeatherHandler())
        listOfHandler.add(QuizHandler(mediaManager))
        listOfHandler.add(QuizAnswerHandler(mediaManager))
        listOfHandler.add(MusicHandler(mediaManager))
        listOfHandler.add(StopMusicHandler(mediaManager))
    }

    /**
     * Check for online intent handlers and handle if possible
     */
    fun handleIntent(intentMessage: DetectIntentResponse): String {

        listOfHandler.forEach {
            if (it.canHandle(intentMessage)) {
                return it.handle(intentMessage)
            }
        }
        return intentMessage.queryResult.fulfillmentText
    }

    /**
     * Check for offline intent handlers and handle if possible
     */
    fun handleOfflineIntent(intentMessage: String): String {

        listOfHandler.forEach {
            if (it.canHandleOffline(intentMessage)) {
                return it.handleOffline(intentMessage)
            }
        }
        return "I understood '${intentMessage}'. Unfortunately I don't know what to do. :("
    }
}