package com.kp.loomo.features.startpage

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
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
import com.kp.loomo.commons.extensions.util.NetworkUtils
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
    private var dialogflowManager: DialogflowManager,
    private var robotManager: RobotManager,
    private var intentHandler: IntentHandler,
    private var timerManager: TimerManager,
    private var googleCloudTTSManager: GoogleCloudTTSManager,
    private var sharedPrefs: SharedPreferences
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

    private var currentResponse: DetectIntentResponse? = null

    private var isOnline = false
    private var onlineServicesInitialized = false

    /**
     * Initialize all speech services
     */
    override fun initSpeech() {
        Log.d(TAG, "initializing speech...")

        robotManager.initRobotConnection(this)
        initAndroidTTS()

        checkInternetConnection()

        if (isOnline) {
            initOnlineServices()
            showText("Manual voice input available, use Loomo for optimal user experience.")
        } else {
            showText("Unfortunately I can't connect to the internet. My functionality might be limited.")
            handler.post {
                startpageFragment?.updateOnlineServicesInitializedView(
                    onlineServicesInitialized
                )
            }
        }
    }

    /**
     * Initialize all online services
     */
    private fun initOnlineServices() {
        dialogflowManager.init(this)
        timerManager.init(this)
        onlineServicesInitialized = true
        handler.post {
            startpageFragment?.updateOnlineServicesInitializedView(
                onlineServicesInitialized
            )
        }
    }

    /**
     * Initialize the built in TTS for offline speech
     */
    private fun initAndroidTTS() {

        mTTS = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->

            if (status == TextToSpeech.SUCCESS) {
                mTTS?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onDone(utteranceId: String) {
                        onSpeechFinished()
                    }

                    override fun onError(utteranceId: String) {}
                    override fun onStart(utteranceId: String) {}
                })
            } else {
                Log.e(TAG, "Initilization Failed!")
            }

            if (status != TextToSpeech.ERROR) {
                mTTS?.language = Locale.US
            }
        })
    }

    /**
     * Initialize manual speech recognition after button click
     */
    override fun initManualSpeech() {
        startAudioRecording()
    }

    /**
     * Start audio recording and analyze it with Google STT
     */
    fun startAudioRecording() {

        checkInternetConnection()

        Log.d(TAG, "Recording ...")
        val timeoutHandler = Handler(Looper.getMainLooper())

        if (isOnline) {

            if (!onlineServicesInitialized) {
                initOnlineServices()
            }

            showText("I'm listening...")
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
                    timeoutHandler.postDelayed({
                        Log.d(TAG, "Timeout")
                        showText("Sorry, I can't hear you.")
                        robotManager.startWakeUpListener()
                        controller?.cancel()
                        onComplete()
                    }, 5000)
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
                    timeoutHandler.removeCallbacksAndMessages(null)
                    handler.post {
                        when {
                            // handle recognized text
                            response!!.resultsCount > 0 -> {
                                startpageFragment?.showText(
                                    response.getResults(0).getAlternatives(
                                        0
                                    ).transcript, OutputView.RSP
                                )
                                //send to Dialogflow
                                dialogflowManager.sendToDialogflow(
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
     * Handle everything after speech is finished
     */
    fun onSpeechFinished() {

        checkInternetConnection()

        if (isOnline) {
            if (!onlineServicesInitialized) {
                initOnlineServices()
            }
            if (currentResponse?.queryResult!!.allRequiredParamsPresent) {
                Log.d(TAG, "All params ready.")
                robotManager.startWakeUpListener()
                currentResponse = null
            } else {
                Log.e(TAG, "Not enough params")
                handler.post{startAudioRecording()}
            }
        } else {
            robotManager.startWakeUpListener()
        }
    }

    /**
     * Handle response from Dialogflow (online)
     */
    override fun handleDialogflowResponse(response: DetectIntentResponse) {
        val botReply: String

        if (response.queryResult.fulfillmentText == "" && response.webhookStatus.code == 4) {
            botReply = "Sorry, I can't answer this right now. Please try again later."
        } else {
            currentResponse = response
            botReply = intentHandler.handleIntent(response)
        }

        startpageFragment?.showText(botReply, OutputView.RSP)

        val enableGoogleCloudTTS = sharedPrefs.getBoolean("google_tts", false)
        speak(botReply, enableGoogleCloudTTS)
    }

    /**
     * Handle response from PocketSphinx (offline)
     */
    override fun handlePocketSphinxResponse(response: String) {

        val botReply: String = if (response == "Timeout") {
            "Sorry, I can't hear you."
        } else {
            intentHandler.handleOfflineIntent(response)
        }

        showText(botReply)
        speak(botReply, false)
    }

    /**
     * Check internet connection and update debug view
     */
    private fun checkInternetConnection() {
        isOnline = NetworkUtils.hasInternetConnection(connectivityManager)
        handler.post { startpageFragment?.updateIsOnlineView(isOnline) }
    }

    /**
     * Show text on screen
     */
    override fun showText(text: String) {
        handler.post { startpageFragment?.showText(text, OutputView.RSP) }
    }

    /**
     * Make the robot speak (both offline and online)
     */
    private fun speak(text: String, onlineTTS: Boolean) {

        checkInternetConnection()

        var speechText = text

        // cut off long responses to save data and time
        if (speechText.length > 200) {
            speechText = speechText.take(200)
        }

        if (onlineTTS && isOnline) {
            googleCloudTTSManager.textToSpeech(speechText, ::onSpeechFinished)
        } else {
            mTTS?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, (0..100).random().toString())
        }
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

    override fun takeView(view: StartpageContract.View) {
        this.startpageFragment = view
    }

    override fun dropView() {
        // cleanup
        mAudioEmitter?.stop()
        mAudioEmitter = null
        if (mSpeechClient != null) {
            mSpeechClient!!.shutdown()
        }
        startpageFragment = null
        pocketSphinxManager.shutdown()

        // release TTS
        if (mTTS != null) {
            mTTS!!.stop()
            mTTS!!.shutdown()
        }
    }
}