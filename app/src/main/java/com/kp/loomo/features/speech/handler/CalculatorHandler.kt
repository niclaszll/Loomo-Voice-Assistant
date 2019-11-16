package com.kp.loomo.features.speech.handler

import ai.snips.hermes.IntentMessage
import com.google.gson.Gson
import com.kp.loomo.features.speech.IntentMessageHandler
import org.json.JSONException
import org.json.JSONObject

class CalculatorHandler : IntentMessageHandler {

    private val tag = "CalculatorHandler"

    override fun canHandle(intentMessage: IntentMessage): Boolean {
        return intentMessage.intent.intentName == "niclaszll:ComputeSum"
    }

    override fun handle(intentMessage: IntentMessage): String {

        var sum = 0.0

        val gson = Gson()
        val json = gson.toJson(intentMessage)

        try {
            val jObject = JSONObject(json)
            val jArray = jObject.getJSONArray("slots")

            for (i in 0 until jArray.length()) {
                try {
                    val slotObject = jArray.getJSONObject(i)
                    val valueObject = slotObject.getJSONObject("value")

                    sum += java.lang.Double.parseDouble(valueObject.getString("value"))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }



        return "${sum.toInt()}, right?"
    }
}