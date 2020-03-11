@file:Suppress("UNREACHABLE_CODE")

package com.kp.loomo.features.intents.handler

import android.content.SharedPreferences
import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat

private val TAG = "CalendarHandler"

class CalendarHandler constructor(private var sharedPrefs: SharedPreferences): IntentMessageHandler {

    private val keywords = arrayOf("appointment","meeting","schedule","make appointment", "make an appointment", "delete the meeting", "delete the appointment",
        "return my appointments", "tell me my schedule", "what is my plan")
    private val times = arrayOf("at 4pm", "tomorrow at 6pm")

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
        val dateTime = intentMessage.queryResult.parameters.fieldsMap["date"]!!.stringValue
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


        for (keyword in keywords) {
            if (keyword == "make" && intentMessage.contains(keyword,  true)) {
                for (time in times) {
                    if (intentMessage.contains(time,  true)) {
                        val value = toTimeNumber(time)
                        val value2 = value + 1
                        val termin = arrayOf("appointment",value, value2 )//later how to change the name

                        return "What should I call it? Got it. Appointment."

                    }
                }
                return "Sorry, couldn't make the appointment"
            } else if (keyword == "delete" && intentMessage.contains(keyword,  true)) {
                for (time in times) {
                    if (intentMessage.contains(time,  true)) {
                        val value = toTimeNumber(time)

                        return "Got it. Delete."
                    }
                }
                return "Sorry, couldn't find the appointment to delete."
            } else if (keyword == "tell" && intentMessage.contains(keyword,  true)) {
                /*
                need of an constructor, where are they found?
                 */
                return "Your appointments:"
            }
        }
        return "Sorry, there has been a mistake."
    }
}
    private fun toTimeNumber(stringNumber: String): Int {
        when (stringNumber) {
            "four pm" -> return 16
            "ten am" -> return 10
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

