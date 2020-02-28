package com.kp.loomo.features.intents.handler

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.kp.loomo.features.intents.IntentMessageHandler
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat

private val TAG = "CalendarHandler"

class CalendarHandler : IntentMessageHandler {

    override fun canHandle(intentMessage: DetectIntentResponse): Boolean {
        // return intentMessage.queryResult.intent.displayName == "Calendar"
        return false
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
        val date1 = intentMessage.queryResult.parameters.fieldsMap["date"]!!.toString()
        val event = intentMessage.queryResult.parameters.fieldsMap["event"]!!.stringValue
        val message = intentMessage.queryResult.fulfillmentText
        return "${date1}, ${event},${message}"
    }


    override fun canHandleOffline(intentMessage: String): Boolean {
        return false
    }

    override fun handleOffline(intentMessage: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

    class appointment{
        var eventl: String? = null
        var date: String? = null

    }
    fun saveAppointment(intentMessage: DetectIntentResponse) : appointment{
        val e = appointment()
        e.eventl = intentMessage.queryResult.parameters.fieldsMap["eventName"]!!.stringValue
        e.date = intentMessage.queryResult.parameters.fieldsMap["dateTime"]!!.stringValue
        println("0")
        val arrayList = ArrayList<String>()

        arrayList.add("00")
        for (i in arrayList) {
            println(i)
        }
        return e
    }


/**
 * Idea: Try to find the date, time and appointment in the speech
 * intent detected --> still a problem
 * with fulfillment no intent detected and it does not work with the code
 * make arrays that can be saved
 * save the times and make a window of 1 hour that has an appointment
 * and after that hour make it free again
 * information can be retrieved
 * and you will be informed if the time slot is not available
 * not to make new calendar? does this already have calendar functions I  can use?
 * none found yet
 * cancel should be available too
 *
 * calendarContract.Instances should save start and end time of occurances and be able to generate reoccuring appointments
 * calendarContract.Attendees guest information?
 * calendarContract.Reminders holds notification data
 * oder array liste, die dann aber mit dem Tag abgleicht
 * also event,tag, zeit müssen da sein für einen Eintrag in die arrayliste
*/

