package com.kp.loomo.features.speech

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse

/**
 * Interface between managers and presenter
 */
interface SpeechResponseHandler {

    fun handleDialogflowResponse(response: DetectIntentResponse)
    fun handlePocketSphinxResponse(response: String)
    fun showText(text: String)
}