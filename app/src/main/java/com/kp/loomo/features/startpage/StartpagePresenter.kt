package com.kp.loomo.features.startpage

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.annotation.Nullable
import com.google.api.gax.rpc.ApiStreamObserver
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.dialogflow.v2beta1.*
import com.google.cloud.speech.v1.*
import com.kp.loomo.R
import com.kp.loomo.di.ActivityScoped
import com.kp.loomo.features.intents.IntentHandler
import com.kp.loomo.features.robot.RobotManager
import com.kp.loomo.features.speech.*
import com.kp.loomo.features.speech.AudioEmitter
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject


private const val TAG = "StartpagePresenter"

/**
 * Logic behind Startpage fragment
 */
@ActivityScoped
class StartpagePresenter @Inject constructor(
    private var applicationContext: Context,
    private var pocketSphinxManager: PocketSphinxManager,
    private var connectivityManager: ConnectivityManager,
    private var dialogFlowManager: DialogFlowManager,
    private var robotManager: RobotManager
) :
    StartpageContract.Presenter, SpeechResponseHandler {

    @Nullable
    private var startpageFragment: StartpageContract.View? = null

    // For main thread work, e.g. UI changes
    private val handler = Handler(Looper.getMainLooper())

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

    private var mTTS: TextToSpeech? = null
    private var isManual = false

    /**
     * Initialize all speech services
     */
    override fun initSpeech() {
        Log.d(TAG, "initializing speech...")

        robotManager.initRobotConnection(this)

        if (hasInternetConnection()) {
            // online
            dialogFlowManager.init(this)

        } else {
            // offline
            pocketSphinxManager.initPocketSphinx(this)
            showText("Say 'activate'")
        }
    }

    /**
     * Initialize Manual speech after button click
     */
    override fun initManualSpeech() {
        isManual = true

        // init TSS
        mTTS = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                //if there is no error then set language
                mTTS?.language = Locale.US
            }
        })

        startAudioRecording()
    }

    /**
     * Start audio recording and analyze it with Google STT
     */
    fun startAudioRecording() {

        Log.d(TAG, "recording ...")
        handler.post { startpageFragment?.showText("I'm listening...") }

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
                                dialogFlowManager.sendToDialogflow(
                                    value.getResults(0).getAlternatives(
                                        0
                                    ).transcript
                                )

                            }
                            else -> Log.d(TAG, "No Response!")
                        }
                    }
                }

                override fun onError(t: Throwable) {
                    Log.e(TAG, "an error occurred", t)
                }

                override fun onCompleted() {
                    Log.d(TAG, "stream closed")
                    mAudioEmitter?.stop()
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
                    .setSingleUtterance(true)
                    .build()
            }

            // send the next request
            requestStream.onNext(builder.build())
        }
    }

    override fun handleDialogflowResponse(response : DetectIntentResponse) {
        val botReply = IntentHandler.handleIntent(response)

        startpageFragment?.showText(botReply)

        if (isManual) {
            mTTS?.speak(botReply, TextToSpeech.QUEUE_FLUSH, null, (0..100).random().toString())
        } else {
            robotManager.speak(botReply)
        }
    }

    override fun handlePocketSphinxResponse(response: String) {
        // handle Pocket Sphinx response
        Log.d(TAG, "Handling PocketSphinx response: $response")
    }

    fun showText (text: String) {
        handler.post { startpageFragment?.showText(text) }
    }

    private fun hasInternetConnection(): Boolean {
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
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

        pocketSphinxManager.shutdown()
    }

}