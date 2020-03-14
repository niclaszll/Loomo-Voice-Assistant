package com.kp.loomo

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.protobuf.Value
import com.kp.loomo.commons.extensions.util.NetworkUtils
import com.kp.loomo.features.intents.handler.OnlineTestHandler
import com.kp.loomo.features.intents.handler.SystemSettingsHandler
import com.kp.loomo.features.robot.SystemSettingsManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class OnlineTestHandlerInstrumentedTest {

    private val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var onlineTestHandler: OnlineTestHandler
    private lateinit var connectivityManager: ConnectivityManager


    private val intentMessagesWrong = arrayOf("follow", "weather")

    private val onlineIntent =
        com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.intentBuilder.setDisplayName("OnlineTest").build()

    private val timerIntent = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
        .queryResultBuilder.intentBuilder.setDisplayName("Timer").build()

    @Before
    fun initClasses() {
        connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        onlineTestHandler = OnlineTestHandler(connectivityManager)
    }

    @Test
    fun testCanHandle_match() {

        // queryResult part of intentMessage
        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(onlineIntent).build()

        // final intentMessage
        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        // pass intentMessage to canHandle()
        val result = onlineTestHandler.canHandle(intentMessage)

        assertTrue(result)
    }

    @Test
    fun testCanHandle_noMatch() {

        // queryResult part of intentMessage
        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(timerIntent).build()

        // final intentMessage
        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        // pass intentMessage to canHandle()
        val result = onlineTestHandler.canHandle(intentMessage)

        assertFalse(result)
    }

    @Test
    fun testHandle() {

        // queryResult part of intentMessage
        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(onlineIntent).build()

        // final intentMessage
        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        val result = onlineTestHandler.handle(intentMessage)

        if (NetworkUtils.hasInternetConnection(connectivityManager)) {
            assertEquals("I am currently online.", result)
        } else {
            assertEquals("I am currently offline.", result)
        }
    }
}
