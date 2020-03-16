package com.kp.loomo

import android.content.Context
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.protobuf.Value
import com.kp.loomo.features.intents.handler.FollowRobotHandler
import com.kp.loomo.features.intents.handler.TimerHandler
import com.kp.loomo.features.robot.RobotManager
import com.kp.loomo.features.robot.TimerManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class FollowRobotHandlerInstrumentedTest {

    private val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var followRobotHandler: FollowRobotHandler
    private lateinit var robotManager: RobotManager

    private val intentMessages = mapOf(
        "follow me" to "Following",
        "start following me" to "Following",
        "start following" to "Following",
        "follow" to "Following",
        "start follow" to "Following",
        "stop following" to "I'm no longer following",
        "stop following me" to "I'm no longer following",
        "stop follow" to "I'm no longer following"
    )
    private val intentMessagesWrong = arrayOf("timer", "weather")

    // Intent with displayName "Follow"
    private val followRobotIntent = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
        .queryResultBuilder.intentBuilder.setDisplayName("Follow").build()

    // Intent with displayName "Timer"
    private val timerIntent = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
        .queryResultBuilder.intentBuilder.setDisplayName("Timer").build()

    @Before
    fun initClasses() {
        robotManager = RobotManager(appContext)
        followRobotHandler = FollowRobotHandler(robotManager)
    }

    @Test
    fun testCanHandle_match() {

        // queryResult part of intentMessage
        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(followRobotIntent).build()

        // final intentMessage
        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        // pass intentMessage to canHandle()
        val result = followRobotHandler.canHandle(intentMessage)

        assertTrue(result)
    }

    @Test
    fun testCanHandle_noMatch() {

        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(timerIntent).build()

        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        val result = followRobotHandler.canHandle(intentMessage)

        assertFalse(result)
    }

    @Test
    fun testHandle_follow() {

        // Parameter Map for parameter builder
        val paramMap = mapOf<String, Value>(
            "FollowCommand" to Value.newBuilder().setStringValue("Start").build()
        )

        // Parameters to put in queryResult
        val params = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.parametersBuilder.putAllFields(paramMap)

        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(followRobotIntent).setParameters(params).build()

        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        val result = followRobotHandler.handle(intentMessage)

        assertEquals("Following", result)
    }

    @Test
    fun testHandle_stopFollow() {

        // Parameter Map for parameter builder
        val paramMap = mapOf<String, Value>(
            "FollowCommand" to Value.newBuilder().setStringValue("Stop").build()
        )

        // Parameters to put in queryResult
        val params = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.parametersBuilder.putAllFields(paramMap)

        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(followRobotIntent).setParameters(params).build()

        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        val result = followRobotHandler.handle(intentMessage)

        assertEquals("I'm no longer following", result)
    }

    @Test
    fun testCanHandleOffline_match() {

        for ((msg,res) in intentMessages) {
            val result = followRobotHandler.canHandleOffline(msg)
            assertTrue(result)
        }
    }

    @Test
    fun testCanHandleOffline_noMatch() {

        for (intentMessage in intentMessagesWrong) {
            val result = followRobotHandler.canHandleOffline(intentMessage)
            assertFalse(result)
        }
    }
}
