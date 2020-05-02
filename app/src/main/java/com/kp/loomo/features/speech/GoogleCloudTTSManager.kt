package com.kp.loomo.features.speech

import android.content.SharedPreferences
import android.media.MediaPlayer
import com.google.gson.Gson
import com.kp.loomo.BuildConfig
import kotlinx.coroutines.runBlocking
import okhttp3.*
import java.io.IOException
import javax.inject.Inject


class GoogleCloudTTSManager @Inject constructor(private var sharedPrefs: SharedPreferences) {

    private var mediaPlayer: MediaPlayer? = null
    private val apiKey = BuildConfig.GoogleSecAPIKEY
    private val url = "https://texttospeech.googleapis.com/v1beta1/text:synthesize"
    private val client = OkHttpClient()

    fun textToSpeech(text: String, callbackOnSpeechFinished: () -> Unit) = runBlocking {

        client.newCall(createRequest(text)).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException) {
                e.printStackTrace()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call?, response: Response) {
                response.body().use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    response.body()?.string().let { body ->
                        val audioResponse = Gson().fromJson(
                            body,
                            AudioResponse::class.java
                        )
                        playAudio(audioResponse, callbackOnSpeechFinished)
                    }
                }
            }
        })
    }

    private fun createRequest(text: String) = Request.Builder()
        .url(url)
        .addHeader("X-Goog-Api-Key", apiKey)
        .addHeader("Content-Type", "application/json; charset=utf-8")
        .post(createRequestBody(text))
        .build()

    private fun createRequestBody(text: String): RequestBody {

        val voiceGender = sharedPrefs.getString("voice_gender", "FEMALE")

        val requestParams = RequestParams(
            input = SynthesisInput(text = text),
            voice = VoiceSelectionParams(languageCode = "en-US", ssmlGender = voiceGender!!),
            audioConfig = AudioConfig(audioEncoding = "LINEAR16")
        )
        val json = Gson().toJson(requestParams)
        return RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            json
        )
    }

    private fun playAudio(audioResponse: AudioResponse, callback: () -> Unit) {
        try {
            val dataSource = "data:audio/mp3;base64,${audioResponse.audioContent}"
            mediaPlayer = MediaPlayer().apply {
                setDataSource(dataSource)
                prepare()
                start()
            }
            mediaPlayer!!.setOnCompletionListener {
                callback()
            }
        } catch (IoEx: IOException) {
            throw IoEx
        }
    }

    data class AudioResponse(
        val audioContent: String?
    )

    data class RequestParams(
        val input: SynthesisInput,
        val voice: VoiceSelectionParams,
        val audioConfig: AudioConfig
    )

    data class SynthesisInput(
        val text: String
    )

    data class VoiceSelectionParams(
        val languageCode: String,
        val ssmlGender: String
    )

    data class AudioConfig(
        val audioEncoding: String
    )
}