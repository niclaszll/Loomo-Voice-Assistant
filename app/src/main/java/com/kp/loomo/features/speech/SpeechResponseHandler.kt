package com.kp.loomo.features.speech

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse


interface SpeechResponseHandler {

    fun handleDialogflowResponse(response: DetectIntentResponse)
    fun handlePocketSphinxResponse(response: String)
}