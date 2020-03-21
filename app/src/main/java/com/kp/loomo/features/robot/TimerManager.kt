package com.kp.loomo.features.robot

import android.content.Context
import android.media.RingtoneManager
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.kp.loomo.features.startpage.StartpagePresenter
import com.kp.loomo.features.startpage.TimerViewCallback
import java.util.*

private const val TAG = "TimerManager"

class TimerManager(private var applicationContext: Context) {

    var activeTimer: CountDownTimer? = null
    var remainingTime = 0
    var paused = false
    var viewCallback: TimerViewCallback? = null
    private val handler = Handler(Looper.getMainLooper())

    fun init(timerViewCallback: TimerViewCallback) {
        viewCallback = timerViewCallback
    }

    fun setTimer(seconds: Int, minutes: Int) {

        paused = false

        val secondsInMillis = seconds * 1000
        val minutesInMillis = minutes * 60 * 1000
        val timerLength = secondsInMillis.toLong() + minutesInMillis.toLong()

        remainingTime = timerLength.toInt()

        handler.post{
            activeTimer = object : CountDownTimer(timerLength, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    Log.d(TAG,"seconds remaining: " + millisUntilFinished / 1000)
                    remainingTime -= 1000
                    viewCallback?.displayTimer(remainingTime/1000)
                }

                override fun onFinish() {
                    Log.d(TAG,"Timer finished")
                    remainingTime = 0
                    viewCallback?.displayTimer(remainingTime)
                    playRing()
                }
            }.start()
        }
    }

    private fun playRing() {

        var alert =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        if (alert == null) {
            // alert is null, using backup
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            if (alert == null) {
                // alert backup is null, using 2nd backup
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }
        }

        val ringtone = RingtoneManager.getRingtone(
            applicationContext,
            alert
        )
        ringtone.play()

        val timer = Timer()
        // stop alarm after 5 seconds
        timer.schedule(object : TimerTask() {
            override fun run() {
                ringtone.stop()
                timer.cancel()
            }
        }, 5000)
    }
}