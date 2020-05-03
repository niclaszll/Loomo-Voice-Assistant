@file:Suppress("UNREACHABLE_CODE")

package com.kp.loomo.features.intents.handler

import android.util.Log
import android.content.SharedPreferences
import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "CalendarHandler"

class CalendarHandler constructor(private var sharedPrefs: SharedPreferences) :
    IntentMessageHandler {

    private val mustHaves = arrayOf("appointment", "meeting", "schedule", "plan")
    private val keywords = arrayOf("make", "create", "delete", "cancel", "return", "tell")
    private val times = arrayOf(
        "one pm", "two pm", "three pm", "four pm", "five pm",
        "six pm", "seven pm", "eight pm", "nine pm", "ten pm",
        "eleven pm", "twelve am", "one am", "two am", "three am", "four am", "five am",
        "six am", "seven am", "eight am", "nine am", "ten am",
        "eleven am", "twelve pm"
    )

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        return intentMessage.queryResult.intent.displayName == "Calendar"

    }

    fun String.getStringDate(
        initialFormat: String,
        requiredFormat: String,
        locale: Locale = Locale.getDefault()
    ): String {
        return this.toDate(initialFormat, locale).toString(requiredFormat, locale)
    }

    private fun String.toDate(format: String, locale: Locale = Locale.getDefault()): Date =
        SimpleDateFormat(format, locale).parse(this)

    private fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    override fun handle(intentMessage: DetectIntentResponse): String {

        val dateTime = intentMessage.queryResult.parameters.fieldsMap["dateTimeStart"]!!.stringValue
        if (dateTime == "") return intentMessage.queryResult.fulfillmentText
        val event = intentMessage.queryResult.parameters.fieldsMap["event"]!!.stringValue
        if (event == "") return intentMessage.queryResult.fulfillmentText
        val eventName = intentMessage.queryResult.parameters.fieldsMap["eventName"]!!.stringValue
        if (eventName == "") return intentMessage.queryResult.fulfillmentText
        return "Got it. $event $eventName on $dateTime"

    }

    override fun canHandleOffline(intentMessage: String): Boolean {
        for (mustHave in mustHaves) {
            if (intentMessage.contains(mustHave, true)) {
                return true
            }
        }
        return false
    }

    override fun handleOffline(intentMessage: String): String {

        val editor = sharedPrefs.edit()

        for (mustHave in mustHaves) {
            for (keyword in keywords) {
                if ((keyword == "make" || keyword == "create") && intentMessage.contains(
                        keyword,
                        true
                    )
                ) {
                    for (time in times) {
                        if (intentMessage.contains(time, true)) {
                            //check if time slot is free
                            val empty = sharedPrefs.getBoolean(toTimeNumber(time).toString(), true)
                            return if (empty) {
                                val startTime = toTimeNumber(time)
                                editor.putBoolean(startTime.toString(), false)
                                editor.apply()
                                "Got it. $mustHave at $time."
                            } else {
                                "Sorry, that time slot is already full"
                            }
                        }
                    }
                    return "Sorry, couldn't make the appointment."
                } else if ((keyword == "delete" || keyword == "cancel") && intentMessage.contains(
                        keyword,
                        true
                    )
                ) {
                    for (time in times) {
                        if (intentMessage.contains(time, true)) {
                            //check if time slot is free
                            val empty = sharedPrefs.getBoolean(toTimeNumber(time).toString(), true)
                            return if (!empty) {
                                val startTime = toTimeNumber(time)
                                editor.putBoolean(startTime.toString(), true)
                                editor.apply()
                                "Got it. Deleted $mustHave at $time."
                            } else {
                                "Sorry, that time slot is empty. Nothing to delete here."
                            }
                        }
                    }
                    return "Sorry, couldn't delete the appointment."
                } else if ((keyword == "return" || keyword == "tell") && intentMessage.contains(
                        keyword,
                        true
                    )
                ) {
                    for (time in times) {
                        if (intentMessage.contains(time, true)) {
                            //check if time slot is free
                            val empty = sharedPrefs.getBoolean(toTimeNumber(time).toString(), true)
                            return if (!empty) {
                                "You got an appointment at $time."
                            } else {
                                "There is no appointment at $time."
                            }
                        }
                    }
                    return "Sorry, couldn't tell if there is an appointment."
                }
            }
        }
        return "Sorry there was an error."
    }

    private fun toTimeNumber(stringNumber: String): Int {
        when (stringNumber) {
            "one am" -> return 1
            "two am" -> return 2
            "three am" -> return 3
            "four am" -> return 4
            "five am" -> return 5
            "six am" -> return 6
            "seven am" -> return 7
            "eight am" -> return 8
            "nine am" -> return 9
            "ten am" -> return 10
            "eleven am" -> return 11
            "twelve pm" -> return 12 //noon
            "one pm" -> return 13
            "two pm" -> return 14
            "three pm" -> return 15
            "four pm" -> return 16
            "five pm" -> return 17
            "six pm" -> return 18
            "seven pm" -> return 19
            "eight pm" -> return 20
            "nine pm" -> return 21
            "ten pm" -> return 22
            "eleven pm" -> return 23
            "twelve am" -> return 0 //midnight
        }
        return 0
    }
}

