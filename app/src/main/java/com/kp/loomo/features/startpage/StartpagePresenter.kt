package com.kp.loomo.features.startpage

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.annotation.Nullable
import com.google.api.gax.rpc.ClientStream
import com.google.api.gax.rpc.ResponseObserver
import com.google.api.gax.rpc.StreamController
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse
import com.google.cloud.speech.v1.*
import com.kp.loomo.R
import com.kp.loomo.di.ActivityScoped
import com.kp.loomo.features.intents.IntentHandler
import com.kp.loomo.features.robot.RobotManager
import com.kp.loomo.features.robot.TimerManager
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
    private var robotManager: RobotManager,
    private var intentHandler: IntentHandler,
    private var timerManager: TimerManager
) :
    StartpageContract.Presenter, SpeechResponseHandler, TimerViewCallback {

    @Nullable
    private var startpageFragment: StartpageContract.View? = null

    // For main thread work, e.g. UI changes
    private val handler = Handler(Looper.getMainLooper())

    // Audio Recording
    private var mAudioEmitter: AudioEmitter? = null

    // Speech client
    private var mSpeechClient: SpeechClient? = null

    private var responseObserver: ResponseObserver<StreamingRecognizeResponse>? = null
    private var requestStream: ClientStream<StreamingRecognizeRequest>? = null

    private var mTTS: TextToSpeech? = null
    private var isManual = false

    private var currentResponse: DetectIntentResponse? = null

    /**
     * Initialize all speech services
     */
    override fun initSpeech() {
        Log.d(TAG, "initializing speech...")

        if (hasInternetConnection()) {
            robotManager.initRobotConnection(this, true)
            // online
            mTTS = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->

                if (status == TextToSpeech.SUCCESS) {
                    mTTS?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onDone(utteranceId: String) {
                            onSpeechFinished(true)
                        }
                        override fun onError(utteranceId: String) {}
                        override fun onStart(utteranceId: String) {}
                    })
                } else {
                    Log.e(TAG, "Initilization Failed!")
                }

                if (status != TextToSpeech.ERROR) {
                    //if there is no error then set language
                    mTTS?.language = Locale.US
                }
            })

            dialogFlowManager.init(this)
            timerManager.init(this)

        } else {
            robotManager.initRobotConnection(this, false)
            // offline
            mTTS = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->

                if (status == TextToSpeech.SUCCESS) {
                    mTTS?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onDone(utteranceId: String) {
                            onSpeechFinished(false)
                        }
                        override fun onError(utteranceId: String) {}
                        override fun onStart(utteranceId: String) {}
                    })
                } else {
                    Log.e(TAG, "Initilization Failed!")
                }

                if (status != TextToSpeech.ERROR) {
                    // if there is no error then set language
                    mTTS?.language = Locale.US

                    // inform user, that loomo is offline
                    val offlineString =
                        "Unfortunately I can't connect to the internet. My functionality might be limited."
                    showText(offlineString)
                    //mTTS?.speak(offlineString, TextToSpeech.QUEUE_FLUSH, null, (0..100).random().toString())
                }
            })


        }
    }

    fun onSpeechFinished (online: Boolean) {
        if(online) {
            if (currentResponse?.queryResult!!.allRequiredParamsPresent) {
                Log.d(TAG, "All params ready.")
                robotManager.startWakeUpListener()
                currentResponse = null
            } else {
                Log.e(TAG, "Not enough params")
                startAudioRecording(true)
            }
        } else {
            robotManager.startWakeUpListener()
        }
    }

    /**
     * Initialize Manual speech after button click
     */
    override fun initManualSpeech() {
        isManual = true

        if (hasInternetConnection()) {
            // online
            startAudioRecording(true)

        } else {
            // offline
            startAudioRecording(false)
        }
    }

    /**
     * Start audio recording and analyze it with Google STT
     */
    fun startAudioRecording(online: Boolean) {

        Log.d(TAG, "recording ...")
        showText("I'm listening...")

        if (online) {
            val isFirstRequest = AtomicBoolean(true)
            mAudioEmitter = AudioEmitter()

            if (mSpeechClient == null) {
                mSpeechClient = SpeechClient.create(SpeechSettings.newBuilder()
                    .setCredentialsProvider {
                        GoogleCredentials.fromStream(
                            applicationContext.resources.openRawResource(
                                R.raw.credential
                            )
                        )
                    }
                    .build())
            }

            responseObserver = object : ResponseObserver<StreamingRecognizeResponse> {

                override fun onStart(controller: StreamController?) {
                    Log.d(TAG, "start stream")
                }

                override fun onError(t: Throwable) {
                    Log.e(TAG, "an error occurred", t)
                }

                override fun onComplete() {
                    Log.d(TAG, "stream closed")
                    mAudioEmitter?.stop()
                    mSpeechClient!!.close()
                    mSpeechClient = null

                }

                override fun onResponse(response: StreamingRecognizeResponse?) {
                    handler.post {
                        when {
                            // handle recognized text
                            response!!.resultsCount > 0 -> {
                                startpageFragment!!.showText(
                                    response.getResults(0).getAlternatives(
                                        0
                                    ).transcript, OutputView.RSP
                                )
                                //send to Dialogflow
                                dialogFlowManager.sendToDialogflow(
                                    response.getResults(0).getAlternatives(
                                        0
                                    ).transcript
                                )
                                // stop audio recording and stream after answer
                                onComplete()
                            }
                        }
                    }

                }
            }

            // start streaming the data to the server and collect responses
            requestStream = mSpeechClient!!.streamingRecognizeCallable()
                .splitCall(responseObserver, null)

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
                requestStream!!.send(builder.build())
            }
        } else {
            pocketSphinxManager.initPocketSphinx(this)
        }


    }

    /**
     * Handle response from Dialogflow (online)
     */
    override fun handleDialogflowResponse(response: DetectIntentResponse) {
        currentResponse = response
        val botReply = intentHandler.handleIntent(response)

        startpageFragment?.showText(botReply, OutputView.RSP)

        //mTTS?.speak(botReply, TextToSpeech.QUEUE_FLUSH, null, (0..100).random().toString())

        GoogleCloudTTSManager().textToSpeech(botReply, ::onSpeechFinished)
    }

    /**
     * Handle response from PocketSphinx (offline)
     */
    override fun handlePocketSphinxResponse(response: String) {
        val botReply = intentHandler.handleOfflineIntent(response)

        showText(botReply)

        mTTS?.speak(botReply, TextToSpeech.QUEUE_FLUSH, null, (0..100).random().toString())
    }

    /**
     * Show text on screen
     */
    override fun showText(text: String) {
        handler.post { startpageFragment?.showText(text, OutputView.RSP) }
        //startpageFragment?.showText(text, OutputView.RSP)
    }

    /**
     * Display timer
     */
    override fun displayTimer(remainingSeconds: Int) {
        if (remainingSeconds == 0) {
            startpageFragment?.showText("", OutputView.ADD)
        } else {
            startpageFragment?.showText(remainingSeconds.toString(), OutputView.ADD)
        }
    }

    /**
     * Check if internet connection available
     */
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
        mSpeechClient!!.shutdown()
        startpageFragment = null
        pocketSphinxManager.shutdown()
        releaseTts()
    }

    private fun releaseTts() {
        if (mTTS != null) {
            mTTS!!.stop()
            mTTS!!.shutdown()
        }
    }
}