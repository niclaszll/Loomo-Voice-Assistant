package com.kp.loomo.features.startpage

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.Nullable
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.api.gax.rpc.ApiStreamObserver
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.dialogflow.v2beta1.*
import com.google.cloud.speech.v1.*
import com.kp.loomo.R
import com.kp.loomo.di.ActivityScoped
import com.kp.loomo.features.speech.AudioEmitter
import com.kp.loomo.features.speech.RequestJavaV2Task
import java.io.InputStream
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

private const val TAG = "StartpagePresenter"

/**
 * Logic behind Startpage fragment
 */
@ActivityScoped
class StartpagePresenter @Inject constructor(private var applicationContext: Context) :
    StartpageContract.Presenter {

    @Nullable
    private var startpageFragment: StartpageContract.View? = null

    // For main thread work, e.g. UI changes
    private val handler = Handler(Looper.getMainLooper())

    // Java V2 Dialogflow
    private var sessionsClient: SessionsClient? = null
    private var session: SessionName? = null
    private val uuid = UUID.randomUUID().toString()

    // Audio Recording
    private var mAudioEmitter: AudioEmitter? = null

    // Speech client
    private val mSpeechClient by lazy {
        applicationContext.resources.openRawResource(R.raw.credential).use {
            SpeechClient.create(
                SpeechSettings.newBuilder()
                    .setCredentialsProvider { GoogleCredentials.fromStream(it) }
                    .build())
        }
    }

    /**
     * Initialize all speech services
     */
    override fun initSpeech() {
        Log.d(TAG, "initializing speech...")
        startAudioRecording()
        initDialogflowClient()
    }

    /**
     * Initialize Dialogflow client
     */
    private fun initDialogflowClient() {
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
     * Start audio recording
     */
    private fun startAudioRecording() {

        val isFirstRequest = AtomicBoolean(true)
        mAudioEmitter = AudioEmitter()

        // start streaming the data to the server and collect responses
        val requestStream = mSpeechClient.streamingRecognizeCallable()
            .bidiStreamingCall(object : ApiStreamObserver<StreamingRecognizeResponse> {
                override fun onNext(value: StreamingRecognizeResponse) {
                    // run on ui thread
                    handler.post {
                        when {
                            // handle recognized text
                            value.resultsCount > 0 -> {
                                startpageFragment!!.showText(
                                    value.getResults(0).getAlternatives(
                                        0
                                    ).transcript
                                )
                                //send to Dialogflow
                                sendToDialogflow(
                                    value.getResults(0).getAlternatives(
                                        0
                                    ).transcript
                                )
                                onCompleted()
                            }
                            else -> Log.d(TAG, "Error!")
                        }
                    }
                }

                override fun onError(t: Throwable) {
                    Log.e(TAG, "an error occurred", t)
                }

                override fun onCompleted() {
                    mAudioEmitter?.stop()
                    mSpeechClient.shutdown()
                    Log.d(TAG, "stream closed")
                }
            })

        // monitor the input stream and send requests as audio data becomes available
        mAudioEmitter?.start { bytes ->
            val builder = StreamingRecognizeRequest.newBuilder()
                .setAudioContent(bytes)

            // if first time, include the config
            if (isFirstRequest.getAndSet(false)) {
                builder.streamingConfig = StreamingRecognitionConfig.newBuilder()
                    .setConfig(
                        RecognitionConfig.newBuilder()
                            .setLanguageCode("en-US")
                            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                            .setSampleRateHertz(16000)
                            .build()
                    )
                    .setInterimResults(false)
                    .setSingleUtterance(false)
                    .build()
            }

            // send the next request
            requestStream.onNext(builder.build())
        }
    }

    /**
     * Handle Dialogflow response
     */
    fun handleDialogflowResponse(response: DetectIntentResponse?) {
        if (response != null) { // process aiResponse here
            val botReply = response.queryResult.fulfillmentText
            startpageFragment?.showText(botReply)
            Log.d(TAG, "V2 Bot Reply: $botReply")
        } else {
            Log.d(TAG, "Bot Reply: Null")
        }
    }

    /**
     * Send to Dialogflow
     */
    private fun sendToDialogflow(msg: String) {
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

    override fun takeView(view: StartpageContract.View) {
        this.startpageFragment = view
    }

    override fun dropView() {
        // cleanup
        mAudioEmitter?.stop()
        mAudioEmitter = null
        mSpeechClient.shutdown()
        startpageFragment = null
    }

}