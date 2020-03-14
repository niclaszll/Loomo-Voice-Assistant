@file:Suppress("UNREACHABLE_CODE")

package com.kp.loomo.features.intents.handler

import android.content.SharedPreferences
import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private val TAG = "CalendarHandler"

class CalendarHandler constructor(private var sharedPrefs: SharedPreferences): IntentMessageHandler {

    private val mustHave = arrayOf("appointment","meeting","schedule", "plan")
    private val keywords = arrayOf("make", "create", "delete", "cancel", "return", "tell")
    private val times = arrayOf("at one pm", "at two pm", "at three pm", "at four pm", "at five pm",
        "at six pm", "at seven pm", "at eight pm", "at nine pm", "at ten pm",
        "at eleven", "twelve am", "am", "pm")

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
         return intentMessage.queryResult.intent.displayName == "Calendar"

    }

    fun String.getStringDate(initialFormat: String, requiredFormat: String, locale: Locale = Locale.getDefault()): String {
        return this.toDate(initialFormat, locale).toString(requiredFormat, locale)
    }

    fun String.toDate(format: String, locale: Locale = Locale.getDefault()): Date = SimpleDateFormat(format, locale).parse(this)

    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
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
        for (keyword in keywords) {
            if (intentMessage.contains(keyword, true)) {
                return true
            }
        }
        return false
    }

    override fun handleOffline(intentMessage: String): String {

        // Initiate Editor Instance to be able to make changes, Änderungen vornehmen zu können
        val editor = sharedPrefs.edit()

        // Irgendwas in den Shared Prefs speichern -> KEY ist Referenz mit der dann später drauf zugegriffen werden kann
        // Save something in Shared Prefs --> KEY being the reference which will grant access later
        // du kannst verschiendene Typen speichern (Bool, String, etc.)
        //editor.putBoolean("KEY", true)
        //editor.putString("KEY", "Der zu speichernde String")
        //editor.apply() // Änderungen committen, commit changes


        // Aus Shared Prefs lesen, defValue wird genommen, falls nichts bei dem Key gespeichert ist
        // Take from Shared Prefs (list from online)
        //val myBool = sharedPrefs.getBoolean("KEY", false)
        //val myString = sharedPrefs.getString("KEY", "Default String")

        //sortedMapOf()
        for (mustHave in mustHave) {
            for (keyword in keywords) {
                if (keyword == "make" || keyword == "create" && intentMessage.contains(
                        keyword, true)) {
                    for (time in times) {
                        if (intentMessage.contains(time, true)) {
                            val startTime = toTimeNumber(time)
                            val endTime = startTime + 1 //ends + 1h later
                            //how to get name
                            val x = mustHave
                            val appMap = mapOf(startTime to x, endTime to "y")
                            //what if the key is time?
                            editor.putBoolean(time, true)
                            editor.putString(time, mustHave.toString() + appMap)
                            editor.apply()
                            return "Got it. $x at $startTime"

                        }
                    }
                    return "Sorry, couldn't make the appointment"
                } else if (keyword == "delete" || keyword == "cancel" && intentMessage.contains(
                        keyword, true)) {
                    for (time in times) {
                        if (intentMessage.contains(time, true)) {
                            val finding = sharedPrefs.getBoolean(time, true)
                            if (finding.equals(true)) {
                                val findString = sharedPrefs.getString(time, "")
                                editor.putString(time, "delete")
                                editor.apply()
                                return "Got it. Delete $findString on $time."
                                //if boolean is true, then the appointment exists and this will change name
                                //else it does not exist and nothing is to be done
                            }
                            //delete --> false?
                            return "No appointment at that time" // if time is recognized, but there is no appointment
                        }
                    }
                    return "Sorry, couldn't understand the time for the appointment." //keyword is true, but no 'time'
                } else if (keyword == "tell" || keyword == "return" && intentMessage.contains(
                        keyword, true)) {
                    for (time in times) {
                        val findings = sharedPrefs.getBoolean(time, true)
                        if(findings.equals(true)) { //in need for a better comparison
                            //if boolean true, then there are appointments for the time
                            val findingsString = sharedPrefs.getString(time, "")
                            return "Your appointment: $findingsString"
                        }
                    }
                    return "No appointments found."
                }
                return "No appointments found."
            }
            return "Sorry, couldn't understand what you want to do."
        }
        return ""
    }
}
    private fun toTimeNumber(stringNumber: String): Int {
        when (stringNumber) {
            "one am"-> return 1
            "two am"-> return 2
            "three am"-> return 3
            "four am"-> return 4
            "five am"-> return 5
            "six am"-> return 6
            "seven am"-> return 7
            "eight am"-> return 8
            "nine am"-> return 9
            "ten am"-> return 10
            "eleven am"-> return 11
            "twelve pm"-> return 12 //noon
            "one pm"-> return 13
            "two pm"-> return 14
            "three pm"-> return 15
            "four pm"-> return 16
            "five pm"-> return 17
            "six pm"-> return 18
            "seven pm"-> return 19
            "eight pm"-> return 20
            "nine pm"-> return 21
            "ten pm"-> return 22
            "eleven pm"-> return 23
            "twelve am"-> return 0 //midnight
        }
        return 0
    }

    /*class appointment{
        var event: String? = null
        var dateTime: String? = null

    }
    fun saveAppointment(intentMessage: DetectIntentResponse) : appointment{
        val e = appointment()
        e.event = intentMessage.queryResult.parameters.fieldsMap["eventName"]!!.stringValue
        e.dateTime = intentMessage.queryResult.parameters.fieldsMap["dateTime"]!!.stringValue
        println("0")
        val arrayList = ArrayList<String>()
        val arrayname = Array(3, { i -> i * 1 }) //0 1 2 3 4
        arrayname.set(0, 3)
        arrayList.add("00")
        for (i in arrayList) {
            println(i)
        }
        return e
    }*/


/**
 * Idea: Try to find the date, time and appointment in the speech
 * make arrays that can be saved
 * save the times and make a window of 1 hour that has an appointment
 * and after that hour make it free again
 * information can be retrieved
 * and you will be informed if the time slot is not available
 * cancel should be available too
*/

