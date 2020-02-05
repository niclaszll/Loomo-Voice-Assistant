package com.kp.loomo.features.robot

import android.content.Context
import android.net.sip.SipErrorCode.TIME_OUT
import android.util.Log
import com.kp.loomo.commons.extensions.util.HeadControlHandlerImpl
import com.kp.loomo.features.startpage.StartpagePresenter
import com.segway.robot.algo.dts.*
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.locomotion.head.Head
import com.segway.robot.sdk.locomotion.sbv.Base
import com.segway.robot.sdk.vision.DTS
import com.segway.robot.sdk.vision.Vision
import com.segway.robot.sdk.voice.Recognizer
import com.segway.robot.sdk.voice.Speaker
import com.segway.robot.sdk.voice.recognition.WakeupListener
import com.segway.robot.sdk.voice.recognition.WakeupResult
import com.segway.robot.sdk.voice.tts.TtsListener
import com.segway.robot.support.control.HeadPIDController
import javax.inject.Inject


private const val TAG = "RobotManager"

class RobotManager @Inject constructor(private var applicationContext: Context) {

    // Loomo SDK
    private var mRecognizer: Recognizer? = null
    private var mSpeaker: Speaker? = null

    // Listeners
    private var mWakeupListener: WakeupListener? = null
    private var mTtsListener: TtsListener? = null
    private var mPersonDetectListener: PersonDetectListener? = null
    private var mPersonTrackingListener: PersonTrackingListener? = null
    private var mPersonTrackingWithPlannerListener: PersonTrackingWithPlannerListener? = null

    private var mHeadPIDController = HeadPIDController()
    private var mVision: Vision? = null
    private var mHead: Head? = null
    private var mBase: Base? = null

    private val mPersonTrackingProfile: PersonTrackingProfile? = null

    private var startTime: Long = 0

    private var mCurrentState: RobotStateType? = null

    enum class RobotStateType {
        INITIATE_DETECT, TERMINATE_DETECT, INITIATE_TRACK, TERMINATE_TRACK
    }

    private var mDts: DTS? = null

    private var isVisionBind = false
    private var isHeadBind = false
    private var isBaseBind = false

    private var startpagePresenter: StartpagePresenter? = null

    fun initRobotConnection(presenter: StartpagePresenter) {
        startpagePresenter = presenter
        initComponents()
        initListeners()
    }

    /**
     * Init Loomo components
     */
    private fun initComponents() {
        mRecognizer = Recognizer.getInstance()
        mBase = Base.getInstance()
        mVision = Vision.getInstance()
        mHead = Head.getInstance()
        mSpeaker = Speaker.getInstance()

        mRecognizer!!.bindService(applicationContext, object : ServiceBinder.BindStateListener {
            override fun onBind() {
                Log.d(TAG, "Recognition service onBind")
                startWakeUpListener()
            }

            override fun onUnbind(s: String) {
                Log.d(TAG, "Recognition service onUnbind")
            }
        })

        mBase!!.bindService(applicationContext, object : ServiceBinder.BindStateListener {
            override fun onBind() {
                isBaseBind = true
                Log.d(TAG, "Base service onBind")
            }

            override fun onUnbind(s: String) {
                isBaseBind = false
                Log.d(TAG, "Base service onUnbind")
            }
        })

        mVision!!.bindService(applicationContext, object : ServiceBinder.BindStateListener {
            override fun onBind() {
                isVisionBind = true
                mDts = mVision!!.dts
                mDts?.setVideoSource(DTS.VideoSource.CAMERA)
                mDts?.start()
                Log.d(TAG, "Vision service onBind")
            }

            override fun onUnbind(s: String) {
                isVisionBind = false
                Log.d(TAG, "Vision service onUnbind")
            }
        })

        mHead!!.bindService(applicationContext, object : ServiceBinder.BindStateListener {
            override fun onBind() {
                isHeadBind = true
                resetHead()
                mHeadPIDController.init(HeadControlHandlerImpl(mHead!!))
                mHeadPIDController.headFollowFactor = 1.0f
                Log.d(TAG, "Head service onBind")
            }

            override fun onUnbind(s: String) {
                isHeadBind = false
                Log.d(TAG, "Head service onUnbind")
            }
        })

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
     * Init listeners
     */
    private fun initListeners() {
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

        mWakeupListener = object : WakeupListener {
            override fun onWakeupResult(wakeupResult: WakeupResult?) {
                //show the wakeup result and wakeup angle.
                Log.d(
                    TAG,
                    "Wakeup result:" + wakeupResult?.result + ", angle " + wakeupResult?.angle
                )
                // actionInitiateTrack()
                startpagePresenter?.startAudioRecording()
            }

            override fun onStandby() {
                Log.d(TAG, "WakeUp onStandby")
                startpagePresenter?.showText("Say 'Ok Loomo!'")
            }

            override fun onWakeupError(error: String?) {
                //show the wakeup error reason.
                Log.d(TAG, "WakeUp onWakeupError")
            }

        }

        mPersonDetectListener = object : PersonDetectListener {
            override fun onPersonDetected(person: Array<DTSPerson>) {
                if (person.isEmpty()) {
                    if (System.currentTimeMillis() - startTime > TIME_OUT) {
                        resetHead()
                    }
                    return
                }
                startTime = System.currentTimeMillis()
                if (isServicesAvailable()) {
                    mHead!!.mode = Head.MODE_ORIENTATION_LOCK
                    mHeadPIDController.updateTarget(
                        person[0].theta.toDouble(),
                        person[0].drawingRect,
                        480
                    )
                }
            }

            override fun onPersonDetectionResult(person: Array<DTSPerson>) {}
            override fun onPersonDetectionError(errorCode: Int, message: String) {
                mCurrentState = null
                Log.d(TAG, "PersonDetectListener: $message")
            }
        }

        // person tracking without obstacle avoidance
        mPersonTrackingListener = object : PersonTrackingListener {
            override fun onPersonTracking(person: DTSPerson) {
                startTime = System.currentTimeMillis()
                if (isServicesAvailable()) {
                    mHead!!.mode = Head.MODE_ORIENTATION_LOCK
                    mHeadPIDController.updateTarget(
                        person.theta.toDouble(),
                        person.drawingRect,
                        480
                    )
                    mBase!!.controlMode = Base.CONTROL_MODE_FOLLOW_TARGET
                    val personDistance = person.distance
                    // There is a bug in DTS, while using person.getDistance(), please check the result
                    // The correct distance is between 0.35 meters and 5 meters
                    if (personDistance > 0.35 && personDistance < 5) {
                        val followDistance = (personDistance - 1.2).toFloat()
                        val theta = person.theta
                        mBase!!.updateTarget(followDistance, theta)
                    }
                }
            }

            override fun onPersonTrackingResult(person: DTSPerson) {}
            override fun onPersonTrackingError(errorCode: Int, message: String) {
                mCurrentState = null
                Log.d(TAG, "PersonTrackingListener: $message")
            }
        }

        // person tracking with obstacle avoidance
        mPersonTrackingWithPlannerListener =
            object : PersonTrackingWithPlannerListener {
                override fun onPersonTrackingWithPlannerResult(
                    person: DTSPerson,
                    baseControlCommand: BaseControlCommand
                ) {
                    startTime = System.currentTimeMillis()
                    mHead!!.mode = Head.MODE_ORIENTATION_LOCK
                    mHeadPIDController.updateTarget(
                        person.theta.toDouble(),
                        person.drawingRect,
                        480
                    )
                    when (baseControlCommand.followState) {
                        BaseControlCommand.State.NORMAL_FOLLOW -> setBaseVelocity(
                            baseControlCommand.linearVelocity,
                            baseControlCommand.angularVelocity
                        )
                        BaseControlCommand.State.HEAD_FOLLOW_BASE -> {
                            mBase!!.controlMode = Base.CONTROL_MODE_FOLLOW_TARGET
                            mBase!!.updateTarget(0f, person.theta)
                        }
                        BaseControlCommand.State.SENSOR_ERROR -> setBaseVelocity(0F, 0F)
                    }
                }

                override fun onPersonTrackingWithPlannerError(
                    errorCode: Int,
                    message: String
                ) {
                    mCurrentState = null
                    Log.d(TAG, "PersonTrackingWithPlannerListener: $message")
                }
            }
    }

    /**
     * Start wakeup mode
     */
    fun startWakeUpListener() {
        if (mRecognizer == null) {
            return
        }
        try {
            mRecognizer!!.startWakeupMode(mWakeupListener)
        } catch (e: Throwable) {
            Log.e(TAG, "Exception: ", e)
        }
    }

    /**
     * Make Loomo speak
     */
    fun speak(text: String) {
        mSpeaker!!.speak(text, mTtsListener!!)
    }

    /**
     * Make Loomo drive
     */
    fun drive(direction: String) {

        mBase?.controlMode = Base.CONTROL_MODE_RAW

        when (direction) {
            "forward" -> {
                mBase?.setLinearVelocity(1.0f)
                mBase?.setAngularVelocity(0.15f)
            }
            "backward" -> {
                mBase?.setLinearVelocity(-1.0f)
                mBase?.setAngularVelocity(-0.15f)
            }
            "right" -> {
                mBase?.setLinearVelocity(0f)
                mBase?.setAngularVelocity(-1.5708f)
            }
            "left" -> {
                mBase?.setLinearVelocity(0f)
                mBase?.setAngularVelocity(1.5708f)
            }
            "turn" -> {
                mBase?.setLinearVelocity(0f)
                mBase?.setAngularVelocity(3.14159f)
            }
        }
    }

    /**
     * Initiate tracking
     */
    fun actionInitiateTrack() {
        if (mCurrentState === RobotStateType.INITIATE_TRACK) {
            return
        } else if (mCurrentState === RobotStateType.INITIATE_DETECT) {
            Log.d(TAG, "Please terminate detecting first.")
            return
        }
        startTime = System.currentTimeMillis()
        mCurrentState = RobotStateType.INITIATE_TRACK
        mDts?.startPersonTracking(null, 60 * 1000 * 1000.toLong(), mPersonTrackingListener)
        Log.d(TAG, "initiate tracking....")
    }

    /**
     * Stop tracking
     */
    fun actionTerminateTrack() {
        if (mCurrentState === RobotStateType.INITIATE_TRACK) {
            mCurrentState = RobotStateType.TERMINATE_TRACK
            mDts!!.stopPersonTracking()
            Log.d(TAG, "terminate tracking....")
        } else {
            Log.d(TAG, "The app is not in tracking mode yet.")
        }
    }

    /**
     * Reset head when timeout
     */
    fun resetHead() {
        mHead!!.mode = Head.MODE_SMOOTH_TACKING
        mHead!!.setWorldYaw(0f)
        mHead!!.setWorldPitch(0.7f)
    }

    /**
     * Check if moving services available
     */
    fun isServicesAvailable(): Boolean {
        return isVisionBind && isHeadBind && isBaseBind
    }

    /**
     * Set Loomos base velocity
     */
    private fun setBaseVelocity(
        linearVelocity: Float,
        angularVelocity: Float
    ) {
        mBase!!.controlMode = Base.CONTROL_MODE_RAW
        mBase!!.setLinearVelocity(linearVelocity)
        mBase!!.setAngularVelocity(angularVelocity)
    }
}