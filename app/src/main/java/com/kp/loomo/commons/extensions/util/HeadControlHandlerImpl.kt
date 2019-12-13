package com.kp.loomo.commons.extensions.util

import com.segway.robot.sdk.locomotion.head.Angle
import com.segway.robot.sdk.locomotion.head.Head

import com.segway.robot.support.control.HeadPIDController


class HeadControlHandlerImpl(head: Head) : HeadPIDController.HeadControlHandler {
    private val mHead: Head = head
    override fun getJointYaw(): Float {
        val angle: Angle = mHead.headJointYaw ?: return 0F
        return angle.angle
    }

    override fun getJointPitch(): Float {
        val angle: Angle = mHead.headJointPitch ?: return 0F
        return angle.angle
    }

    override fun setYawAngularVelocity(velocity: Float) {
        mHead.setYawAngularVelocity(velocity)
    }

    override fun setPitchAngularVelocity(velocity: Float) {
        mHead.setPitchAngularVelocity(velocity)
    }

}