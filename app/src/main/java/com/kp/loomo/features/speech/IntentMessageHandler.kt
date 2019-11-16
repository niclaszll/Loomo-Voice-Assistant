package com.kp.loomo.features.speech

import ai.snips.hermes.IntentMessage

interface IntentMessageHandler {

    fun canHandle(intentMessage: IntentMessage): Boolean
    fun handle(intentMessage: IntentMessage): String
}