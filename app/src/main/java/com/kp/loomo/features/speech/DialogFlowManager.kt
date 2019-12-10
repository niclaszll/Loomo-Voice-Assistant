package com.kp.loomo.features.speech

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.dialogflow.v2beta1.*
import com.kp.loomo.R
import com.kp.loomo.features.startpage.StartpagePresenter
import java.io.InputStream
import java.util.*
import javax.inject.Inject

private const val TAG = "DialogflowManager"

class DialogFlowManager @Inject constructor(private var applicationContext: Context) {

    // Java V2 Dialogflow
    private var sessionsClient: SessionsClient? = null
    private var session: SessionName? = null
    private val uuid = UUID.randomUUID().toString()

    private var startpagePresenter: StartpagePresenter? = null

    /**
     * Initialize Dialogflow client
     */
    fun init(presenter: StartpagePresenter) {

        Log.d(TAG, "initializing Dialogflow client ...")

        startpagePresenter = presenter

        try {
            val stream: InputStream = applicationContext.resources.openRawResource(R.raw.credential)
            val credentials = GoogleCredentials.fromStream(stream)
            val projectId = (credentials as ServiceAccountCredentials).projectId
            val settingsBuilder =
                SessionsSettings.newBuilder()
            val sessionsSettings =
                settingsBuilder.setCredentialsProvider(
                    FixedCredentialsProvider.create(credentials)
                ).build()
            sessionsClient =
                SessionsClient.create(sessionsSettings)
            session = SessionName.of(projectId, uuid)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Handle Dialogflow response
     */
    fun handleDialogflowResponse(response: DetectIntentResponse?) {
        Log.d(TAG, "handling Dialogflow response")

        if (response != null) {

            startpagePresenter?.handleResponse(response)

        } else {
            Log.d(TAG, "Dialogflow Response is Null")
        }
    }

    /**
     * Send to Dialogflow
     */
    fun sendToDialogflow(msg: String) {

        Log.d(TAG, "sending message: '$msg' to Dialogflow")

        if (msg.trim { it <= ' ' }.isEmpty()) {
            Toast.makeText(applicationContext, "Please enter your query!", Toast.LENGTH_LONG).show()
        } else {
            val queryInput =
                QueryInput.newBuilder().setText(
                    TextInput.newBuilder().setText(msg).setLanguageCode(
                        "en-US"
                    )
                ).build()
            RequestJavaV2Task(this, session!!, sessionsClient!!, queryInput).execute()
        }
    }
}