package com.kp.loomo.features.intents

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse

interface IntentMessageHandler {

    fun canHandle(intentMessage: DetectIntentResponse): Boolean
    fun handle(intentMessage: DetectIntentResponse): String

    fun canHandleOffline(intentMessage: String): Boolean
    fun handleOffline(intentMessage: String): String
}