package com.kp.loomo.features.robot

import android.content.Context
import android.media.MediaPlayer
import com.kp.loomo.R
import javax.inject.Inject


private const val TAG = "MediaManager"

class MediaManager @Inject constructor(private var applicationContext: Context) {

    private var mediaPlayer: MediaPlayer? = null

    /**
     * Play sound from res directory Int is raw resource id R.raw.file
     */
    fun playLocalSound(file: Int) {
        mediaPlayer = MediaPlayer.create(applicationContext, file)
        mediaPlayer?.start()
    }

    /**
     * Play a sound from an Url
     */
    fun playSoundFromUrl(url: String) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepare()
            start()
        }
    }

    /**
     * Reset the MediaManager
     */
    fun resetPlayer() {
        mediaPlayer?.reset()
        mediaPlayer?.release()
        mediaPlayer = null
    }

}