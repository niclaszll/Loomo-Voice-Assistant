package com.kp.loomo

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.protobuf.Value
import com.kp.loomo.features.intents.handler.MoveRobotHandler
import com.kp.loomo.features.robot.RobotManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MoveRobotHandlerInstrumentedTest {

    private val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var moveRobotHandler: MoveRobotHandler
    private lateinit var robotManager: RobotManager

    private val moveCommands = mapOf(
        "forward" to "Driving forward",
        "backward" to "Driving backward",
        "left" to "Driving left",
        "right" to "Driving right"
    )

    private val intentMessages = mapOf(
        "drive forward" to "Driving forward",
        "drive left" to "Driving left",
        "drive right" to "Driving right",
        "drive backward" to "Driving backward",
        "move forward" to "Driving forward",
        "move left" to "Driving left",
        "move right" to "Driving right",
        "move backward" to "Driving backward",
        "go forward" to "Driving forward",
        "go left" to "Driving left",
        "go right" to "Driving right",
        "go backward" to "Driving backward",
        "turn" to "Ok, turning around"
    )

    private val intentMessagesWrong = arrayOf("timer", "weather")

    // Intent with displayName "moveRobot"
    private val moveRobotIntent = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
        .queryResultBuilder.intentBuilder.setDisplayName("moveRobot").build()

    // Intent with displayName "Timer"
    private val timerIntent = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
        .queryResultBuilder.intentBuilder.setDisplayName("Timer").build()

    @Before
    fun initClasses() {
        robotManager = RobotManager(appContext)
        moveRobotHandler = MoveRobotHandler(robotManager)
    }

    @Test
    fun testCanHandle_match() {

        // queryResult part of intentMessage
        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(moveRobotIntent).build()

        // final intentMessage
        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        // pass intentMessage to canHandle()
        val result = moveRobotHandler.canHandle(intentMessage)

        assertTrue(result)
    }

    @Test
    fun testCanHandle_noMatch() {

        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(timerIntent).build()

        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        val result = moveRobotHandler.canHandle(intentMessage)

        assertFalse(result)
    }

    @Test
    fun testHandle_move() {

        for ((cmd, res) in moveCommands) {

            val paramMap = mapOf<String, Value>(
                "direction" to Value.newBuilder().setStringValue(cmd).build()
            )
            val params = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
                .queryResultBuilder.parametersBuilder.putAllFields(paramMap)

            val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
                .queryResultBuilder.setIntent(moveRobotIntent).setParameters(params).build()

            val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
                .setQueryResult(queryResult).build()

            val result = moveRobotHandler.handle(intentMessage)

            assertEquals(res, result)
        }
    }

    @Test
    fun testCanHandleOffline_match() {

        for ((msg,res) in intentMessages) {
            val result = moveRobotHandler.canHandleOffline(msg)
            assertTrue(result)
        }
    }

    @Test
    fun testCanHandleOffline_noMatch() {

        for (intentMessage in intentMessagesWrong) {
            val result = moveRobotHandler.canHandleOffline(intentMessage)
            assertFalse(result)
        }
    }

    @Test
    fun testHandleOffline() {

        for ((msg,res) in intentMessages) {
            val result = moveRobotHandler.handleOffline(msg)
            assertEquals(res, result)
        }
    }
}
