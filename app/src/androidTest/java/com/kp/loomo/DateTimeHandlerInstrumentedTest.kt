package com.kp.loomo

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kp.loomo.features.intents.handler.DateTimeHandler
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.text.DateFormat
import java.util.*


@RunWith(AndroidJUnit4::class)
class DateTimeHandlerInstrumentedTest {

    private lateinit var timeHandler: DateTimeHandler
    private lateinit var currentTime: Date
    private lateinit var formatter: DateFormat
    private val intentMessages = arrayOf(
        "how late is it",
        "what time is it",
        "time",
        "how late",
        "how late is it now",
        "what time is it now"
    )
    private val intentMessagesWrong = arrayOf("follow", "weather")

    // Intent with displayName "Time"
    private val timeIntent = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
        .queryResultBuilder.intentBuilder.setDisplayName("Time").build()

    // Intent with displayName "Timer"
    private val timerIntent = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
        .queryResultBuilder.intentBuilder.setDisplayName("Timer").build()

    @Before
    fun initClasses() {
        timeHandler = DateTimeHandler()
        formatter = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US)
    }

    @Test
    fun testCanHandle_match() {

        // queryResult part of intentMessage
        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(timeIntent).build()

        // final intentMessage
        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        // pass intentMessage to canHandle()
        val result = timeHandler.canHandle(intentMessage)

        assertTrue(result)
    }

    @Test
    fun testCanHandle_noMatch() {

        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(timerIntent).build()

        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        val result = timeHandler.canHandle(intentMessage)

        assertFalse(result)
    }

    @Test
    fun testHandle() {

        currentTime = Calendar.getInstance().time

        // queryResult part of intentMessage
        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(timeIntent).build()

        // final intentMessage
        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        val result = timeHandler.handle(intentMessage)

        assertEquals("The current time is ${formatter.format(currentTime)}", result)
    }

    @Test
    fun testCanHandleOffline_match() {

        for (intentMessage in intentMessages) {
            val result = timeHandler.canHandleOffline(intentMessage)
            assertTrue(result)
        }
    }

    @Test
    fun testCanHandleOffline_noMatch() {

        for (intentMessage in intentMessagesWrong) {
            val result = timeHandler.canHandleOffline(intentMessage)
            assertFalse(result)
        }
    }

    @Test
    fun testHandleOffline() {

        currentTime = Calendar.getInstance().time

        for (intentMessage in intentMessages) {
            val result = timeHandler.handleOffline(intentMessage)
            assertEquals("The current time is ${formatter.format(currentTime)}", result)
        }
    }
}
