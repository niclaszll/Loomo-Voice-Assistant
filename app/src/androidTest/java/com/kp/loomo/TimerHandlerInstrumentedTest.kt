package com.kp.loomo

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.cloud.dialogflow.v2.DetectIntentResponse
import com.kp.loomo.features.intents.handler.TimerHandler
import com.kp.loomo.features.robot.TimerManager

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class TimerHandlerInstrumentedTest {

    private val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun testCanHandle_true() {

        val intent = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.intentBuilder.setDisplayName("Timer").build()
        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(intent).build()
        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        val timerManager = TimerManager(appContext)
        val timerHandler = TimerHandler(timerManager)

        val result = timerHandler.canHandle(intentMessage)

        assertTrue(result)
    }

    @Test
    fun testCanHandle_false() {

        val intent = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.intentBuilder.setDisplayName("Calendar").build()
        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(intent).build()
        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        val timerManager = TimerManager(appContext)
        val timerHandler = TimerHandler(timerManager)

        val result = timerHandler.canHandle(intentMessage)

        assertFalse(result)
    }
}
