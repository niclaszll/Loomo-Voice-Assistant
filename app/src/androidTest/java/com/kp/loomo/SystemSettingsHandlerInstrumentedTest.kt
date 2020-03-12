package com.kp.loomo

import android.app.Activity
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.protobuf.Value
import com.kp.loomo.features.intents.handler.SystemSettingsHandler
import com.kp.loomo.features.robot.SystemSettingsManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class SystemSettingsHandlerInstrumentedTest {

    private val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var systemSettingsHandler: SystemSettingsHandler
    private lateinit var systemSettingsManager: SystemSettingsManager

    private val systemSettingsIntent =
        com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.intentBuilder.setDisplayName("SystemSettings").build()

    private val timerIntent = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
        .queryResultBuilder.intentBuilder.setDisplayName("Timer").build()

    @Before
    fun initClasses() {
        systemSettingsManager = SystemSettingsManager(appContext)
        systemSettingsManager.setupSystemManager(null)
        systemSettingsHandler = SystemSettingsHandler(systemSettingsManager)
    }

    @Test
    fun testCanHandle_match() {

        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(systemSettingsIntent).build()

        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        val result = systemSettingsHandler.canHandle(intentMessage)

        assertTrue(result)
    }

    @Test
    fun testCanHandle_noMatch() {

        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(timerIntent).build()

        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        val result = systemSettingsHandler.canHandle(intentMessage)

        assertFalse(result)
    }

    @Test
    fun testHandle_brightness() {

        // Parameter Map for parameter builder
        val paramMap = mapOf<String, Value>(
            "SystemSettingsCmd" to Value.newBuilder().setStringValue("brightness").build(),
            "percentage" to Value.newBuilder().setStringValue("40%").build()
        )

        // Parameters to put in queryResult
        val params = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.parametersBuilder.putAllFields(paramMap)

        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(timerIntent).setParameters(params).build()

        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        val result = systemSettingsHandler.handle(intentMessage)

        assertEquals("Setting brightness to 40%", result)
    }
}
