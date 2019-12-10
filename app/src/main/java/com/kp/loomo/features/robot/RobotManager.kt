package com.kp.loomo.features.robot

import android.content.Context
import android.util.Log
import com.kp.loomo.features.startpage.StartpagePresenter
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.voice.Recognizer
import com.segway.robot.sdk.voice.Speaker
import com.segway.robot.sdk.voice.recognition.WakeupListener
import com.segway.robot.sdk.voice.recognition.WakeupResult
import com.segway.robot.sdk.voice.tts.TtsListener
import javax.inject.Inject

private const val TAG = "RobotManager"

class RobotManager @Inject constructor(private var applicationContext: Context) {

    // Loomo SDK
    private var mRecognizer: Recognizer? = null
    private var mSpeaker: Speaker? = null
    private var mWakeupListener: WakeupListener? = null
    private var mTtsListener: TtsListener? = null

    private var startpagePresenter: StartpagePresenter? = null

    fun initRobotConnection (presenter: StartpagePresenter) {
        startpagePresenter = presenter
        initWakeUp()
        initRecognizer()
        initSpeaker()
    }

    /**
     * Init Loomo recognizer
     */
    private fun initRecognizer() {
        mRecognizer = Recognizer.getInstance()
        mRecognizer!!.bindService(applicationContext, object : ServiceBinder.BindStateListener {
            override fun onBind() {
                Log.d(TAG, "Recognition service onBind")
                startWakeUpListener()
            }

            override fun onUnbind(s: String) {
                Log.d(TAG, "Recognition service onUnbind")
            }
        })
    }

    /**
     * Init Loomo speaker
     */
    private fun initSpeaker() {

        mTtsListener = object : TtsListener {
            override fun onSpeechStarted(s: String) {
                //s is speech content, callback this method when speech is starting.
                Log.d(TAG, "onSpeechStarted() called with: s = [$s]")
            }

            override fun onSpeechFinished(s: String) {
                //s is speech content, callback this method when speech is finish.
                Log.d(TAG, "onSpeechFinished() called with: s = [$s]")
                startWakeUpListener()
            }

            override fun onSpeechError(
                s: String,
                s1: String
            ) {
                //s is speech content, callback this method when speech error occurs .
                Log.d(
                    TAG,
                    "onSpeechError() called with: s = [$s], s1 = [$s1]"
                )
            }
        }

        mSpeaker = Speaker.getInstance()
        mSpeaker!!.bindService(applicationContext, object : ServiceBinder.BindStateListener {
            override fun onBind() {
                Log.d(TAG, "Speaker service onBind")
                mSpeaker!!.setVolume(50)
            }

            override fun onUnbind(s: String) {
                Log.d(TAG, "Speaker service onUnbind")
            }
        })
    }


    /**
     * Init Loomo wakeup
     */
    private fun initWakeUp() {
        mWakeupListener = object : WakeupListener {
            override fun onWakeupResult(wakeupResult: WakeupResult?) {
                //show the wakeup result and wakeup angle.
                Log.d(
                    TAG,
                    "Wakeup result:" + wakeupResult?.result + ", angle " + wakeupResult?.angle
                )
                // TODO ?
                //handler.post {
                    startpagePresenter?.startAudioRecording()
                //}
            }

            override fun onStandby() {
                Log.d(TAG, "WakeUp onStandby")
                startpagePresenter?.showText("Say 'Ok Loomo!'") }

            override fun onWakeupError(error: String?) {
                //show the wakeup error reason.
                Log.d(TAG, "WakeUp onWakeupError")
            }

        }
    }

    fun speak (text : String) {
        mSpeaker!!.speak(text, mTtsListener!!)
    }

    private fun startWakeUpListener() {
        if (mRecognizer == null) {
            return
        }
        try {
            mRecognizer!!.startWakeupMode(mWakeupListener)
        } catch (e: Throwable) {
            Log.e(TAG, "Exception: ", e)
        }
    }
}