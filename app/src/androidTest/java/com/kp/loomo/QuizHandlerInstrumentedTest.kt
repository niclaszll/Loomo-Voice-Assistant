package com.kp.loomo

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kp.loomo.features.intents.handler.QuizHandler
import com.kp.loomo.features.robot.MediaManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class QuizHandlerInstrumentedTest {

    private val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var quizHandler: QuizHandler
    private lateinit var mediaManager: MediaManager


    private val intentMessages = arrayOf(
        "quiz",
        "another quiz",
        "Brain training",
        "question",
        "questions",
        "Give me another question",
        "a new riddle",
        "quiz questions",
        "Memory training",
        "I want to train my brain"
    )
    private val intentMessagesWrong = arrayOf("timer", "moveRobot")

    // Intent with displayName "Quiz"
    private val quizIntent = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
        .queryResultBuilder.intentBuilder.setDisplayName("Quiz").build()

    // Intent with displayName "Timer"
    private val timerIntent = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
        .queryResultBuilder.intentBuilder.setDisplayName("Timer").build()

    @Before
    fun initClasses() {
        mediaManager = MediaManager(appContext)
        quizHandler = QuizHandler(mediaManager)
    }

    @Test
    fun testCanHandle_match() {

        // queryResult part of intentMessage
        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(quizIntent).build()

        // final intentMessage
        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        // pass intentMessage to canHandle()
        val result = quizHandler.canHandle(intentMessage)

        assertTrue(result)
    }

    @Test
    fun testCanHandle_noMatch() {

        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(timerIntent).build()

        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        val result = quizHandler.canHandle(intentMessage)

        assertFalse(result)
    }

    @Test
    fun testHandle() {

        // queryResult part of intentMessage
        val queryResult = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .queryResultBuilder.setIntent(quizIntent).build()

        // final intentMessage
        val intentMessage = com.google.cloud.dialogflow.v2beta1.DetectIntentResponse.newBuilder()
            .setQueryResult(queryResult).build()

        val result = quizHandler.handle(intentMessage)

        //result is random question or error text
        assertNotNull(result)
    }

    @Test
    fun testCanHandleOffline_match() {

        for (intentMessage in intentMessages) {
            val result = quizHandler.canHandleOffline(intentMessage)
            assertTrue(result)
        }
    }

    @Test
    fun testCanHandleOffline_noMatch() {

        for (intentMessage in intentMessagesWrong) {
            val result = quizHandler.canHandleOffline(intentMessage)
            assertFalse(result)
        }
    }

    @Test
    fun testHandleOffline() {

        for (intentMessage in intentMessages) {
            val result = quizHandler.handleOffline(intentMessage)
            assertTrue(result.startsWith("You are offline. So I just have a task for you. Calculate"))
        }
    }
}
