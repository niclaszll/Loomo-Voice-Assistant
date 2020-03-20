package com.kp.loomo.features.speech

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import edu.cmu.pocketsphinx.*
import java.io.File
import java.io.IOException
import javax.inject.Inject

private const val TAG = "PocketSphinx Manager"

class PocketSphinxManager @Inject constructor(private var applicationContext: Context) :
    RecognitionListener {

    private var intentSearch = "intent"
    private var recognizer: SpeechRecognizer? = null
    private var responseHandler: SpeechResponseHandler? = null

    /**
     * Initialize PocketSphinx
     */
    fun initPocketSphinx(handler: SpeechResponseHandler) {

        Log.d(TAG, "Initializing PocketSphinx ...")
        responseHandler = handler
        runRecognizerSetup()
    }

    /**
     * Recognizer initialization is a time-consuming and it involves IO,
     * so we execute it in async task
     */
    @SuppressLint("StaticFieldLeak")
    private fun runRecognizerSetup() {

        object : AsyncTask<Void?, Void?, Exception?>() {
            override fun doInBackground(vararg params: Void?): Exception? {
                try {
                    val assets = Assets(applicationContext)
                    val assetDir: File = assets.syncAssets()
                    setupRecognizer(assetDir)
                } catch (e: IOException) {
                    return e
                }
                return null
            }

            override fun onPostExecute(result: Exception?) {
                if (result != null) {
                    Log.d(TAG, result.message!!)
                } else {
                    startNewSearch(intentSearch)
                }
            }
        }.execute()
    }

    /**
     * Setup recognizers with listeners
     */
    @Throws(IOException::class)
    private fun setupRecognizer(assetsDir: File) {
        recognizer = SpeechRecognizerSetup.defaultSetup()
            .setAcousticModel(File(assetsDir, "test_model"))
            .setDictionary(
                File(
                    assetsDir,
                    "cmudict-en-us.dict"
                )
            )
            .setSampleRate(16000)
            .recognizer

        recognizer?.addListener(this)

        // Create custom grammar-based search
        val intentGrammar = File(assetsDir, "intents.gram")
        recognizer?.addGrammarSearch(intentSearch, intentGrammar)

        Log.d(TAG, "recording ...")
        responseHandler?.showText("I'm listening...")
    }


    /**
     * Start listening for intents
     */
    private fun startNewSearch(searchName: String) {
        recognizer?.stop()
        recognizer?.startListening(searchName, 5000)
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    override fun onPartialResult(hypothesis: Hypothesis?) {
        if (hypothesis == null) return
        Log.d(TAG, "onPartialResult: " + hypothesis.hypstr)
    }

    /**
     * Handle detected result/hypothesis
     */
    override fun onResult(hypothesis: Hypothesis?) {
        if (hypothesis != null) {
            Log.d(TAG, "onResult " + hypothesis.hypstr)
            recognizer?.stop()
            responseHandler?.handlePocketSphinxResponse(hypothesis.hypstr)
        }
    }

    /**
     * Start listening again if timeout
     */
    override fun onTimeout() {
        Log.d(TAG, "Speech timeout")
        recognizer?.stop()
        responseHandler?.handlePocketSphinxResponse("Timeout")
        //startNewSearch(intentSearch)
    }

    override fun onBeginningOfSpeech() {}

    override fun onEndOfSpeech() {
        if (recognizer?.searchName.equals(intentSearch)) startNewSearch(intentSearch)
    }

    override fun onError(error: Exception) {
        Log.d(TAG, error.message!!)
    }

    /**
     * Shutdown PocketSphinx
     */
    fun shutdown() {
        if (recognizer != null) {
            recognizer?.cancel()
            recognizer?.shutdown()
        }
    }
}