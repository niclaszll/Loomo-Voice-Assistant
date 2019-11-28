package com.kp.loomo.features.startpage

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.annotation.Nullable
import com.kp.loomo.R
import com.kp.loomo.di.ActivityScoped
import com.segway.robot.sdk.base.bind.ServiceBinder.BindStateListener
import com.segway.robot.sdk.voice.Recognizer
import com.segway.robot.sdk.voice.Speaker
import com.segway.robot.sdk.voice.VoiceException
import com.segway.robot.sdk.voice.audiodata.RawDataListener
import com.segway.robot.sdk.voice.recognition.WakeupListener
import com.segway.robot.sdk.voice.recognition.WakeupResult
import java.io.*
import java.util.*
import java.util.zip.ZipInputStream
import javax.inject.Inject
import kotlin.concurrent.schedule

/*
/**
 * Logic behind Startpage fragment
 */
@ActivityScoped
class StartpagePresenter2 @Inject constructor(private var applicationContext: Context) : StartpageContract.Presenter {

    @Nullable
    private var startpageFragment: StartpageContract.View? = null

    private val tag = "StartpagePresenter"
    private lateinit var mTTS: TextToSpeech

    lateinit var assistantLocation: File

    private var mRecognizer: Recognizer? = null
    private val mSpeaker: Speaker? = null
    private var mWakeupListener: WakeupListener? = null
    private val mRawDataListener: RawDataListener? = null

    override fun takeView(view: StartpageContract.View) {
        this.startpageFragment = view
    }

    override fun dropView() {
        startpageFragment = null
    }

    /**
     * Initialize SST and TTS
     */
    override fun initSpeech() {

        Log.d("tag", applicationContext.filesDir.toString())
        Log.d("ManActivity", Environment.getExternalStorageDirectory().toString())

        assistantLocation = File(applicationContext.filesDir, "snips")
        extractAssistantIfNeeded(assistantLocation)

        // init Snips
        initWakeUp()
        initRecognizer()

        // init TTS
        mTTS = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR){
                //if there is no error then set language
                mTTS.language = Locale.US
            }
        })
    }

    /**
     * Start snips client
     */
    override fun startSnips() {
        val client = createClient(assistantLocation)
        client.connect(applicationContext)
    }

    /**
     * Extracts the snips assistant if not already existent
     */
    private fun extractAssistantIfNeeded(assistantLocation: File) {
        val versionFile = File(
            assistantLocation,
            "android_version_" + BuildConfig.VERSION_NAME
        )

        if (versionFile.exists()) {
            return
        }

        try {
            assistantLocation.delete()
            this.unzip(
                applicationContext.assets.open("assistant.zip"),
                assistantLocation
            )
            versionFile.createNewFile()
        } catch (e: IOException) {
            return
        }

    }

    /**
     * Create Snips client and register callbacks
     */
    private fun createClient(assistantLocation: File): SnipsPlatformClient {
        val assistantDir = File(assistantLocation, "assistant")

        val client = SnipsPlatformClient.Builder(assistantDir)
            .enableDialogue(true)
            .enableHotword(false)
            .enableSnipsWatchHtml(false)
            .enableLogs(true)
            .withHotwordSensitivity(0.5f)
            .enableStreaming(true)
            .enableInjection(false)
            .build()

        /**
         * Called when client ready for voice command
         */
        client.onPlatformReady = fun() {
            Log.d(tag, "Snips is ready. Say the wake word!")
            startpageFragment!!.showText(R.string.instruction)
            return
        }

        /**
         * Called on error
         */
        client.onPlatformError =
            fun(snipsPlatformError: SnipsPlatformClient.SnipsPlatformError) {
                // Handle error
                Log.d(tag, "Error: " + snipsPlatformError.message!!)
                startpageFragment!!.showText(R.string.error_text)
                return
            }

        /**
         * Called when hotword is detected
         */
        client.onHotwordDetectedListener = fun() {
            // Wake word detected, start a dialog session
            Log.d(tag, "Wake word detected!")
            client.startSession(
                null, ArrayList(),
                false, null
            )

            startpageFragment!!.showText(R.string.listening_text)
        }

        /**
         * Called when intent is detected
         */
        client.onIntentDetectedListener = @SuppressLint("SetTextI18n")
        fun(intentMessage: IntentMessage) {

            // Intent detected, so the dialog session ends here
            client.endSession(intentMessage.sessionId, null)
            Log.d(tag, "Intent detected: " + intentMessage.intent.intentName)

            // get answer from IntentHandler
            val answer = IntentHandler.handleIntent(intentMessage)

            // render answer and give voice feedback
            startpageFragment!!.showText(answer)
            if (answer == ""){
                // if there is no text
                Log.d(tag, "Empty answer :(")
            }
            else{
                // if there is text
                mTTS.speak(answer, TextToSpeech.QUEUE_FLUSH, null, (0..100).random().toString())
            }

        }

        client.onSnipsWatchListener = fun(s: String) {
            Log.d(tag, "Log: $s")
        }

        return client
    }

    /**
     * Unzip helper function
     * TODO: move to commons
     */
    private fun unzip(zipFile: InputStream, targetDirectory: File) {
        ZipInputStream(BufferedInputStream(zipFile)).use { zis ->
            var ze = zis.nextEntry
            var count: Int
            val buffer = ByteArray(8192)

            while (ze != null) {
                val file = File(targetDirectory, ze.name)
                println("unzipping ${file.absoluteFile}")
                val dir = if (ze.isDirectory) file else file.parentFile
                if (!dir.isDirectory && !dir.mkdirs())
                    throw FileNotFoundException("Failed to ensure directory: " + dir.absolutePath)
                if (ze.isDirectory) {
                    ze = zis.nextEntry
                    continue
                }
                FileOutputStream(file).use { fout ->
                    count = zis.read(buffer)
                    while (count != -1) {
                        fout.write(buffer, 0, count)
                        count = zis.read(buffer)
                    }
                }
                ze = zis.nextEntry
            }
        }
    }

    private fun initRecognizer() {
        mRecognizer = Recognizer.getInstance()
        mRecognizer!!.bindService(applicationContext, object : BindStateListener {
            override fun onBind() {
                Log.d(tag, "Recognition service onBind")
                startWakeUpListener()
            }

            override fun onUnbind(s: String) {
                Log.d(tag, "Recognition service onUnbind")
                //speaker service or recognition service unbind, disable function buttons.
            }
        })
    }


    private fun initWakeUp() {
        mWakeupListener = object : WakeupListener {
            override fun onStandby() {
                Log.d(tag, "WakeUp onStandby")
            }

            override fun onWakeupResult(wakeupResult: WakeupResult) { //show the wakeup result and wakeup angle.
                Log.d(
                    tag,
                    "Wakeup result:" + wakeupResult.result + ", angle " + wakeupResult.angle
                )
                //start azure recognition
                //azureSpeechRecognition.getRecognitionClientWithIntent().startMicAndRecognition()
                val message = handler.obtainMessage()
                message.sendToTarget()
            }

            override fun onWakeupError(s: String) { //show the wakeup error reason.
                Log.d(tag, "WakeUp onWakeupError")

            }
        }
    }

    fun startWakeUpListener() {
        if (mRecognizer == null) {
            return
        }
        try {
            mRecognizer!!.startWakeupMode(mWakeupListener)
        } catch (e: VoiceException) {
            Log.e(tag, "Exception: ", e)
        }
    }

    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        /*
         * handleMessage() defines the operations to perform when
         * the Handler receives a new Message to process.
         */
        override fun handleMessage(inputMessage: Message) {
            // Gets the image task from the incoming Message object.
            startSnips()

        }


    }

}
*/