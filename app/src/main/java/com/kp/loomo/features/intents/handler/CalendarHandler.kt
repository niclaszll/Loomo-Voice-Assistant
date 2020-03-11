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
    private val keywords = arrayOf("appointment","meeting","schedule", "plan","make appointment", "make an appointment", "delete the meeting", "delete the appointment",
        "return my appointments", "tell me my schedule", "what is my plan", "What do I have to do?")
    private val times = arrayOf("at 4pm", "tomorrow at 6pm", "today")

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
        val dateTime = intentMessage.queryResult.parameters.fieldsMap["dateTime"]!!.stringValue
        val event = intentMessage.queryResult.parameters.fieldsMap["event"]!!.stringValue
       /* val message = intentMessage.queryResult.fulfillmentText
        */
        return "Got it. $event on $dateTime"
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
        editor.putBoolean("KEY", true)
        editor.putString("KEY", "Der zu speichernde String")
        editor.apply() // Änderungen committen, commit changes


        // Aus Shared Prefs lesen, defValue wird genommen, falls nichts bei dem Key gespeichert ist
        // Take from Shared Prefs (list from online)
        val myBool = sharedPrefs.getBoolean("KEY", false)
        val myString = sharedPrefs.getString("KEY", "Default String")

        //sortedMapOf()
        for (keyword in keywords) {
            if (keyword == "make" && intentMessage.contains(keyword,  true)) {
                for (time in times) {
                    if (intentMessage.contains(time,  true)) {
                        val startTime = toTimeNumber(time)
                        val endTime = startTime + 1 //ends + 1h later
                        //how to get name
                        val x = mustHave
                        val appMap = mapOf(startTime to x, endTime to "y")
                        //what if the key is time?
                        editor.putBoolean(time, true)
                        editor.putString(time, mustHave.toString()+appMap)
                        editor.apply()
                        return "Got it. $x at $startTime"

                    }
                }
                return "Sorry, couldn't make the appointment"
            } else if (keyword == "delete" || keyword=="cancel"&& intentMessage.contains(keyword,  true)) {
                for (time in times) {
                    if (intentMessage.contains(time,  true)) {
                        val finding = sharedPrefs.getBoolean(time, true)
                        if (finding.equals(true)){
                        editor.putString(time, "delete")
                        editor.apply()
                        }
                        //delete --> false?

                        return "Got it. Delete appointment on $time."
                    }
                }
                return "Sorry, couldn't find the appointment to delete."
            } else if (keyword == "tell"||keyword=="what do I have to do"&& intentMessage.contains(keyword,  true)) {
                for (time in times) {
                    val findings = sharedPrefs.getBoolean(time, true)
                        return "Your appointments: $findings"
                }
            }
            return "No appointments found."
        }
        return "Sorry, there has been a mistake."
    }
}
    private fun toTimeNumber(stringNumber: String): Int {
        when (stringNumber) {
            "1am"-> return 1
            "2am"-> return 2
            "3am"-> return 3
            "4am"-> return 4
            "5am"-> return 5
            "6am"-> return 6
            "7am"-> return 7
            "8am"-> return 8
            "9am"-> return 9
            "10am"-> return 10
            "11am"-> return 11
            "12pm"-> return 12 //noon
            "1pm"-> return 13
            "2pm"-> return 14
            "3pm"-> return 15
            "4pm"-> return 16
            "5pm"-> return 17
            "6pm"-> return 18
            "7pm"-> return 19
            "8pm"-> return 20
            "9pm"-> return 21
            "10pm"-> return 22
            "11pm"-> return 23
            "12am"-> return 0 //midnight


            //more of 4:00 to 0400 and back
            //get the said time

        }
        return 0
    }

    class appointment{
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
    }


/**
 * Idea: Try to find the date, time and appointment in the speech
 * make arrays that can be saved
 * save the times and make a window of 1 hour that has an appointment
 * and after that hour make it free again
 * information can be retrieved
 * and you will be informed if the time slot is not available
 * cancel should be available too
*/

