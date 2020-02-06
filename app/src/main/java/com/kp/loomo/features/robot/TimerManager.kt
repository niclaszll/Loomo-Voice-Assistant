package com.kp.loomo.features.robot

import android.os.CountDownTimer
import android.util.Log

private const val TAG = "TimerManager"

class TimerManager {

    fun setTimer(seconds: Int, minutes: Int) {

        val secondsInMillis = seconds * 1000
        val minutesInMillis = minutes * 60 * 1000
        val timerLength = secondsInMillis.toLong() + minutesInMillis.toLong()

        object : CountDownTimer(timerLength, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d(TAG,"seconds remaining: " + millisUntilFinished / 1000)
            }

            override fun onFinish() {
                Log.d(TAG,"Timer finished")
            }
        }.start()
    }
}