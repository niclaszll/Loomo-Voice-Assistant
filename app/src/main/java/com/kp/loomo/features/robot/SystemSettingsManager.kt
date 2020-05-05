package com.kp.loomo.features.robot

import android.content.Context
import android.media.AudioManager
import android.util.Log
import android.view.Window
import android.view.WindowManager
import javax.inject.Inject
import kotlin.math.roundToInt


class SystemSettingsManager @Inject constructor(applicationContext: Context) {

    private var window: Window? = null
    private val audioManager =
        applicationContext.getSystemService(
            Context.AUDIO_SERVICE
        ) as AudioManager

    /**
     * Setup the SystemManager with default values for brightness and volume
     */
    fun setupSystemManager(w: Window?) {
        this.window = w
        if (window != null) {
            setBrightness(100)
            setAudioVolume(80)
        }
    }

    /**
     * Set the window brightness
     */
    fun setBrightness(value: Int) {
        val brightness = value / 255.toFloat()
        if (window != null) {
            val lp: WindowManager.LayoutParams = window!!.attributes
            lp.screenBrightness = brightness
            window!!.attributes = lp
        }
    }

    /**
     * Set the audio volume
     */
    fun setAudioVolume(volume: Int) {
        // music
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        Log.d("SystemManager", maxVolume.toString())
        val newVolume = (volume.toFloat() / 100) * maxVolume.toFloat()
        Log.d("SystemManager", newVolume.toString())
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume.roundToInt(), 0)

        // ringtone
        val maxVolumeRing = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
        Log.d("SystemManager", maxVolumeRing.toString())
        val newVolumeRing = (volume.toFloat() / 100) * maxVolumeRing.toFloat()
        Log.d("SystemManager", newVolumeRing.toString())
        audioManager.setStreamVolume(AudioManager.STREAM_RING, newVolumeRing.roundToInt(), 0)
    }
}