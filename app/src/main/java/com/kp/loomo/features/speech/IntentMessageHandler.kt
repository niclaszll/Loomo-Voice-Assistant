package com.kp.loomo.features.speech

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse

interface IntentMessageHandler {

    fun canHandle(intentMessage: DetectIntentResponse): Boolean
    fun handle(intentMessage: DetectIntentResponse): String
}