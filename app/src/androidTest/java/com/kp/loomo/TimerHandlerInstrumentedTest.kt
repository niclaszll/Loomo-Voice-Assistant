package com.kp.loomo

import android.content.Context
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.protobuf.Value
import com.kp.loomo.features.intents.handler.TimerHandler
import com.kp.loomo.features.robot.TimerManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class TimerHandlerInstrumentedTest {

    private val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var timerManager: TimerManager
    private lateinit var timerHandler: TimerHandler

    // Intent with displayName "Timer"
    private val timerIntent = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
        .queryResultBuilder.intentBuilder.setDisplayName("Timer").build()

    // Intent with displayName "TimerCommand"
    private val timerCommandIntent = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
        .queryResultBuilder.intentBuilder.setDisplayName("TimerCommand").build()

    // Intent with displayName "Calendar"
    private val calendarIntent = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
        .queryResultBuilder.intentBuilder.setDisplayName("Calendar").build()
    
    @Before
    fun initClasses() {
        timerManager = TimerManager(appContext)
        timerHandler = TimerHandler(timerManager)
    }

    @Test
    fun testCanHandle_match() {

        // queryResult part of intentMessage
        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(timerIntent).build()

        // final intentMessage
        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        // pass intentMessage to canHandle()
        val result = timerHandler.canHandle(intentMessage)

        assertTrue(result)
    }

    @Test
    fun testCanHandle_noMatch() {

        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(calendarIntent).build()

        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        val result = timerHandler.canHandle(intentMessage)

        assertFalse(result)
    }

    @Test
    fun testHandle_onlySeconds() {

        // Parameter Map for parameter builder
        val paramMap = mapOf<String,Value>("seconds" to Value.newBuilder().setNumberValue(10.0).build(), "minutes" to Value.newBuilder().setNumberValue(0.0).build())

        // Parameters to put in queryResult
        val params = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.parametersBuilder.putAllFields(paramMap)

        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(timerIntent).setParameters(params).build()

        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        val result = timerHandler.handle(intentMessage)

        assertEquals("Setting timer for 10 seconds.", result)
        assertNotEquals("Setting timer for 11 seconds.", result)
    }

    @Test
    fun testHandle_onlyMinutes() {

        // Parameter Map for parameter builder
        val paramMap = mapOf<String,Value>("seconds" to Value.newBuilder().setNumberValue(0.0).build(), "minutes" to Value.newBuilder().setNumberValue(21.0).build())

        // Parameters to put in queryResult
        val params = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.parametersBuilder.putAllFields(paramMap)

        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(timerIntent).setParameters(params).build()

        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        val result = timerHandler.handle(intentMessage)

        assertEquals("Setting timer for 21 minutes.", result)
        assertNotEquals("Setting timer for 5 minutes.", result)
    }

    @Test
    fun testHandle_secondsAndMinutes() {

        // Parameter Map for parameter builder
        val paramMap = mapOf<String,Value>("seconds" to Value.newBuilder().setNumberValue(34.0).build(), "minutes" to Value.newBuilder().setNumberValue(2.0).build())

        // Parameters to put in queryResult
        val params = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.parametersBuilder.putAllFields(paramMap)

        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(timerIntent).setParameters(params).build()

        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        val result = timerHandler.handle(intentMessage)

        assertEquals("Setting timer for 2 minutes and 34 seconds.", result)
        assertNotEquals("Setting timer for 3 minutes and 22 seconds.", result)
    }

    @Test
    fun testHandle_pauseTimer() {

        timerManager.setTimer(0, 1)

        // because of async creation
        Thread.sleep(200)

        // Parameter Map for parameter builder
        val paramMap = mapOf<String,Value>("timerCommand" to Value.newBuilder().setStringValue("pause").build())

        // Parameters to put in queryResult
        val params = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.parametersBuilder.putAllFields(paramMap)

        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(timerCommandIntent).setParameters(params).build()

        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        val result = timerHandler.handle(intentMessage)

        assertEquals("Current timer paused.", result)
    }

    @Test
    fun testHandle_resumeTimer() {

        timerManager.setTimer(0, 1)

        // because of async creation
        Thread.sleep(200)

        timerManager.paused = true
        timerManager.activeTimer!!.cancel()

        // Parameter Map for parameter builder
        val paramMap = mapOf<String,Value>("timerCommand" to Value.newBuilder().setStringValue("resume").build())

        // Parameters to put in queryResult
        val params = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.parametersBuilder.putAllFields(paramMap)

        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(timerCommandIntent).setParameters(params).build()

        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        val result = timerHandler.handle(intentMessage)

        assertEquals("Resuming timer with ${timerManager.remainingTime / 1000} seconds remaining.", result)
    }

    @Test
    fun testHandle_stopTimer() {

        timerManager.setTimer(10, 0)

        // Parameter Map for parameter builder
        val paramMap = mapOf<String,Value>("timerCommand" to Value.newBuilder().setStringValue("stop").build())

        // Parameters to put in queryResult
        val params = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.parametersBuilder.putAllFields(paramMap)

        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(timerCommandIntent).setParameters(params).build()

        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        val result = timerHandler.handle(intentMessage)

        assertEquals("Current timer canceled.", result)
    }



}
