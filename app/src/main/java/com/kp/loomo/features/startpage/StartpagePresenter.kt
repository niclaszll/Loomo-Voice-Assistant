package com.kp.loomo.features.startpage

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
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
import com.kp.loomo.features.speech.IntentHandler
import com.kp.loomo.features.speech.PocketSphinxManager
import com.kp.loomo.features.speech.RequestJavaV2Task
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.voice.Recognizer
import com.segway.robot.sdk.voice.Speaker
import com.segway.robot.sdk.voice.VoiceException
import com.segway.robot.sdk.voice.recognition.WakeupListener
import com.segway.robot.sdk.voice.recognition.WakeupResult
import com.segway.robot.sdk.voice.tts.TtsListener
import java.io.InputStream
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject


private const val TAG = "StartpagePresenter"

/**
 * Logic behind Startpage fragment
 */
@ActivityScoped
class StartpagePresenter @Inject constructor(private var applicationContext: Context, private var pocketSphinxManager: PocketSphinxManager, private var connectivityManager: ConnectivityManager) :
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

    // Loomo SDK
    private var mRecognizer: Recognizer? = null
    private var mSpeaker: Speaker? = null
    private var mWakeupListener: WakeupListener? = null
    private var mTtsListener: TtsListener? = null

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

        if (hasInternetConnection()) {
            // online
            initWakeUp()
            initRecognizer()
            initDialogflowClient()
            initSpeaker()

        } else {
            // offline
            pocketSphinxManager.runRecognizerSetup()
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
     * Initialize Dialogflow client
     */
    private fun initDialogflowClient() {

        Log.d(TAG, "initializing Dialogflow client ...")

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
                                sendToDialogflow(
                                    value.getResults(0).getAlternatives(
                                        0
                                    ).transcript
                                )

                            }
                            else -> Log.d(TAG, "Error!")
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

    /**
     * Handle Dialogflow response
     */
    fun handleDialogflowResponse(response: DetectIntentResponse?) {
        Log.d(TAG, "handling Dialogflow response")

        if (response != null) {
            //val botReply = response.queryResult.fulfillmentText
            val botReply = IntentHandler.handleIntent(response)
            startpageFragment?.showText(botReply)

            if (isManual) {
                mTTS?.speak(botReply, TextToSpeech.QUEUE_FLUSH, null, (0..100).random().toString())
            } else {
                mSpeaker!!.speak(botReply, mTtsListener!!)
            }
            Log.d(TAG, "Dialogflow Response: $botReply")
        } else {
            Log.d(TAG, "Dialogflow Response: Null")
        }
    }

    /**
     * Send to Dialogflow
     */
    private fun sendToDialogflow(msg: String) {

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

    /**
     * Init Loomo recognizer
     */
    private fun initRecognizer() {
        mRecognizer = Recognizer.getInstance()
        mRecognizer!!.bindService(applicationContext, object : ServiceBinder.BindStateListener {
            override fun onBind() {
                Log.d(TAG, "Recognition service onBind")
                startWakeUpListener()
            }

            override fun onUnbind(s: String) {
                Log.d(TAG, "Recognition service onUnbind")
            }
        })
    }

    /**
     * Init Loomo speaker
     */
    private fun initSpeaker() {

        mTtsListener = object : TtsListener {
            override fun onSpeechStarted(s: String) { //s is speech content, callback this method when speech is starting.
                Log.d(TAG, "onSpeechStarted() called with: s = [$s]")
            }

            override fun onSpeechFinished(s: String) { //s is speech content, callback this method when speech is finish.
                Log.d(TAG, "onSpeechFinished() called with: s = [$s]")
                startWakeUpListener()
            }

            override fun onSpeechError(
                s: String,
                s1: String
            ) { //s is speech content, callback this method when speech occurs error.
                Log.d(
                    TAG,
                    "onSpeechError() called with: s = [$s], s1 = [$s1]"
                )
            }
        }

        mSpeaker = Speaker.getInstance()
        mSpeaker!!.bindService(applicationContext, object : ServiceBinder.BindStateListener {
            override fun onBind() {
                Log.d(TAG, "Speaker service onBind")
                mSpeaker!!.setVolume(50)
            }

            override fun onUnbind(s: String) {
                Log.d(TAG, "Speaker service onUnbind")
            }
        })
    }


    /**
     * Init Loomo wakeup
     */
    private fun initWakeUp() {
        mWakeupListener = object : WakeupListener {
            override fun onStandby() {
                Log.d(TAG, "WakeUp onStandby")
                handler.post { startpageFragment?.showText("Say 'Ok Loomo!'") }
            }

            override fun onWakeupResult(wakeupResult: WakeupResult) {
                //show the wakeup result and wakeup angle.
                Log.d(
                    TAG,
                    "Wakeup result:" + wakeupResult.result + ", angle " + wakeupResult.angle
                )
                handler.post {
                    startAudioRecording()
                }
            }

            override fun onWakeupError(s: String) {
                //show the wakeup error reason.
                Log.d(TAG, "WakeUp onWakeupError")

            }
        }
    }

    private fun startWakeUpListener() {
        if (mRecognizer == null) {
            return
        }
        try {
            mRecognizer!!.startWakeupMode(mWakeupListener)
        } catch (e: VoiceException) {
            Log.e(TAG, "Exception: ", e)
        }
    }

    private fun hasInternetConnection (): Boolean {
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