package com.kp.loomo.features.speech

import android.os.AsyncTask
import com.google.cloud.dialogflow.v2beta1.*

/**
 * Class for creating Dialogflow session client
 */
class RequestJavaV2Task internal constructor(
    private var dialogflowManager: DialogflowManager,
    private val session: SessionName,
    private val sessionsClient: SessionsClient,
    private val queryInput: QueryInput
) :
    AsyncTask<Void?, Void?, DetectIntentResponse?>() {

    /**
     * Background task to build session client
     */
    override fun doInBackground(vararg params: Void?): DetectIntentResponse? {
        try {
            val detectIntentRequest =
                DetectIntentRequest.newBuilder()
                    .setSession(session.toString())
                    .setQueryInput(queryInput)
                    .build()
            return sessionsClient.detectIntent(detectIntentRequest)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Handle the response after execution
     */
    override fun onPostExecute(response: DetectIntentResponse?) {
        dialogflowManager.handleDialogflowResponse(response)
    }

}
