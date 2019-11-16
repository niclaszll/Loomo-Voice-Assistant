package com.kp.loomo.features.startpage

import ai.snips.hermes.IntentMessage
import ai.snips.platform.BuildConfig
import ai.snips.platform.SnipsPlatformClient
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import com.kp.loomo.R
import com.kp.loomo.di.ActivityScoped
import com.kp.loomo.features.speech.IntentHandler
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.inject.Inject

@ActivityScoped
class StartpagePresenter @Inject constructor(private var applicationContext: Context) : StartpageContract.Presenter {

    @Nullable
    private var startpageFragment: StartpageContract.View? = null

    private val tag = "StartpagePresenter"
    private lateinit var mTTS: TextToSpeech

    lateinit var assistantLocation: File

    override fun takeView(view: StartpageContract.View) {
        this.startpageFragment = view
    }

    override fun dropView() {
        startpageFragment = null
    }

    override fun initSpeech() {
        assistantLocation = File(applicationContext.filesDir, "snips")
        extractAssistantIfNeeded(assistantLocation)

        startSnips()

        mTTS = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR){
                //if there is no error then set language
                mTTS.language = Locale.US
            }
        })
    }

    override fun startSnips() {
        val client = createClient(assistantLocation)
        client.connect(this.applicationContext)
    }

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

    private fun createClient(assistantLocation: File): SnipsPlatformClient {
        val assistantDir = File(assistantLocation, "assistant")

        val client = SnipsPlatformClient.Builder(assistantDir)
            .enableDialogue(true)
            .enableHotword(true)
            .enableSnipsWatchHtml(false)
            .enableLogs(true)
            .withHotwordSensitivity(0.5f)
            .enableStreaming(false)
            .enableInjection(false)
            .build()

        client.onPlatformReady = fun() {
            Log.d(tag, "Snips is ready. Say the wake word!")
            startpageFragment!!.showText(R.string.instruction)
            return
        }

        client.onPlatformError =
            fun(snipsPlatformError: SnipsPlatformClient.SnipsPlatformError) {
                // Handle error
                Log.d(tag, "Error: " + snipsPlatformError.message!!)
                startpageFragment!!.showText(R.string.error_text)
                return
            }

        client.onHotwordDetectedListener = fun() {
            // Wake word detected, start a dialog session
            Log.d(tag, "Wake word detected!")
            client.startSession(
                null, ArrayList(),
                false, null
            )

            startpageFragment!!.showText(R.string.listening_text)
            return
        }

        client.onIntentDetectedListener = @SuppressLint("SetTextI18n")
        fun(intentMessage: IntentMessage) {
            // Intent detected, so the dialog session ends here
            client.endSession(intentMessage.sessionId, null)
            Log.d(tag, "Intent detected: " + intentMessage.intent.intentName)

            val answer = IntentHandler.handleIntent(intentMessage)
            startpageFragment!!.showText(answer)
            if (answer == ""){
                //if there is no text
                Log.d(tag, "Empty answer :(")
            }
            else{
                //if there is text
                mTTS.speak(answer, TextToSpeech.QUEUE_FLUSH, null, (0..100).random().toString())
            }

        }

        client.onSnipsWatchListener = fun(s: String) {
            Log.d(tag, "Log: $s")
        }

        return client
    }

    @Throws(IOException::class)
    private fun unzip(zipFile: InputStream, targetDirectory: File) {
        val zis = ZipInputStream(BufferedInputStream(zipFile))
        zis.use { z ->
            var ze: ZipEntry?
            var count: Int
            val buffer = ByteArray(8192)

            do {
                ze = z.nextEntry
                if (ze == null) {
                    break
                }
                val file = File(targetDirectory, ze.name)
                val dir = if (ze.isDirectory) file else file.parentFile
                if (!dir!!.isDirectory && !dir.mkdirs())
                    throw FileNotFoundException("Failed to make directory: " + dir.absolutePath)
                if (ze.isDirectory)
                    continue
                val fout = FileOutputStream(file)
                fout.use { f ->
                    do {
                        count = z.read(buffer)
                        if (count == -1) {
                            break
                        }
                        f.write(buffer, 0, count)
                    } while (true)
                }

            } while (true)
        }
    }

}