package com.kp.loomo.features.speech

import android.os.AsyncTask
import com.google.cloud.dialogflow.v2beta1.*

class RequestJavaV2Task internal constructor(
    private var dialogFlowManager: DialogFlowManager,
    private val session: SessionName,
    private val sessionsClient: SessionsClient,
    private val queryInput: QueryInput
) :
    AsyncTask<Void?, Void?, DetectIntentResponse?>() {

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

    override fun onPostExecute(response: DetectIntentResponse?) {
        dialogFlowManager.handleDialogflowResponse(response)
    }

}
