package com.kp.loomo.features.robot

import android.content.Context
import android.media.AudioManager
import android.util.Log
import android.view.Window
import android.view.WindowManager
import javax.inject.Inject
import kotlin.math.roundToInt


class SystemSettingsManager @Inject constructor(private var applicationContext: Context) {

    private var window: Window? = null
    private val audioManager =
        applicationContext.getSystemService(
            Context.AUDIO_SERVICE
        ) as AudioManager

    fun setupSystemManager(w: Window) {
        //setup brightness control
        this.window = w
        setBrightness(100)
        setAudioVolume(80)
    }

    fun setBrightness(value: Int) {
        val brightness = value / 255.toFloat()
        val lp: WindowManager.LayoutParams = window!!.attributes
        lp.screenBrightness = brightness
        window!!.attributes = lp
    }

    fun setAudioVolume(volume: Int) {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        Log.d("SystemManager", maxVolume.toString())
        val newVolume = (volume.toFloat() / 100) * maxVolume.toFloat()
        Log.d("SystemManager", newVolume.toString())
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume.roundToInt(), 0)
    }
}