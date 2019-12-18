package com.kp.loomo.features.speech

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import edu.cmu.pocketsphinx.*
import java.io.File
import java.io.IOException
import javax.inject.Inject

private const val TAG = "PocketSphinx"

class PocketSphinxManager @Inject constructor(private var applicationContext: Context) :
    RecognitionListener {

    private var intentSearch = "intent"

    /* Recognition object */
    private var recognizer: SpeechRecognizer? = null

    private var responseHandler: SpeechResponseHandler? = null


    fun initPocketSphinx(handler: SpeechResponseHandler) {

        Log.d(TAG, "initializing PocketSphinx ...")
        responseHandler = handler

        runRecognizerSetup()
    }

    @SuppressLint("StaticFieldLeak")
    fun runRecognizerSetup() {
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
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

    @Throws(IOException::class)
    private fun setupRecognizer(assetsDir: File) {
        recognizer = SpeechRecognizerSetup.defaultSetup()
            .setAcousticModel(File(assetsDir, "en-us-ptm"))
            .setDictionary(
                File(
                    assetsDir,
                    "cmudict-en-us.dict"
                )
            )
            .recognizer

        recognizer?.addListener(this)

        // Create custom grammar-based search
        val intentGrammar = File(assetsDir, "intents.gram")
        recognizer?.addGrammarSearch(intentSearch, intentGrammar)
    }


    private fun startNewSearch(searchName: String) {
        recognizer?.stop()
        recognizer?.startListening(searchName, 10000)
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    override fun onPartialResult(hypothesis: Hypothesis?) {
        if (hypothesis == null) return
        Log.d(TAG, "onPartialResult: " + hypothesis.hypstr)
        startNewSearch(intentSearch)
    }

    override fun onResult(hypothesis: Hypothesis?) {
        if (hypothesis != null) {
            Log.d(TAG, "onResult " + hypothesis.hypstr)
            Log.d(TAG, "onResult " + hypothesis.hypstr)
            recognizer?.stop()
            responseHandler?.handlePocketSphinxResponse(hypothesis.hypstr)
        }
    }

    override fun onError(error: Exception) {
        Log.d(TAG, error.message!!)
    }

    override fun onTimeout() {
        startNewSearch(intentSearch)
    }

    override fun onBeginningOfSpeech() {}

    override fun onEndOfSpeech() {
        if (recognizer?.searchName.equals(intentSearch)) startNewSearch(intentSearch)
    }

    fun shutdown() {
        if (recognizer != null) {
            recognizer?.cancel()
            recognizer?.shutdown()
        }
    }
}