package com.kp.loomo.di

import android.content.Context
import com.kp.loomo.features.robot.RobotManager
import com.kp.loomo.features.speech.DialogFlowManager
import com.kp.loomo.features.speech.PocketSphinxManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SpeechModule {

    @Provides
    @Singleton
    fun providePocketSphinx(context: Context): PocketSphinxManager {
        return PocketSphinxManager(context)
    }

    @Provides
    @Singleton
    fun provideDialogflow(context: Context): DialogFlowManager {
        return DialogFlowManager(context)
    }

    // TODO move to extra module for robot
    @Provides
    @Singleton
    fun provideRobotManager(context: Context): RobotManager {
        return RobotManager(context)
    }
}