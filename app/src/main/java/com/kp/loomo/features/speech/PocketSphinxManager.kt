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

    /* We only need the keyphrase to start recognition, one menu with list of choices,
   and one word that is required for method switchSearch - it will bring recognizer
   back to listening for the keyphrase*/
    private var keywordSearch = "wakeup"
    private var menuSearch = "menu"

    /* Keyword we are looking for to activate recognition */
    private var keyphrase = "activate"

    /* Recognition object */
    private var recognizer: SpeechRecognizer? = null

    // TODO: make toplevel class
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
                    switchSearch(keywordSearch)
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
            // Disable this line if you don't want recognizer to save raw
            // audio files to app's storage
            //.setRawLogDir(assetsDir)
            .recognizer

        recognizer?.addListener(this)
        // Create keyword-activation search.
        recognizer?.addKeyphraseSearch(keywordSearch, keyphrase)
        // Create your custom grammar-based search
        val menuGrammar = File(assetsDir, "mymenu.gram")
        recognizer?.addGrammarSearch(menuSearch, menuGrammar)
    }


    private fun switchSearch(searchName: String) {
        recognizer?.stop()
        if (searchName == keywordSearch) {
            recognizer?.startListening(searchName)
        } else {
            recognizer?.startListening(searchName, 10000)
        }
    }

    override fun onPartialResult(hypothesis: Hypothesis?) {
        if (hypothesis == null) return
        when (hypothesis.hypstr) {
            keyphrase -> {
                switchSearch(menuSearch)
            }
            "hello" -> {
                Log.d(TAG, "Hello to you too!")
            }
            "good morning" -> {
                Log.d(TAG, "Good morning to you too!")
            }
            else -> {
                Log.d(TAG, "onPartialResult: " + hypothesis.hypstr)
            }
        }
    }

    override fun onEndOfSpeech() {
        recognizer?.cancel()
        //if (recognizer?.searchName.equals(KWS_SEARCH)) switchSearch(KWS_SEARCH)
    }

    override fun onResult(hypothesis: Hypothesis?) {
        if (hypothesis != null) {
            Log.d(TAG, "onResult " + hypothesis.hypstr)
        }
    }

    override fun onError(error: Exception) {
        Log.d(TAG, error.message!!)
    }

    override fun onTimeout() {
        switchSearch(keywordSearch)
    }

    override fun onBeginningOfSpeech() {}

    fun shutdown() {
        if (recognizer != null) {
            recognizer?.cancel()
            recognizer?.shutdown()
        }
    }
}