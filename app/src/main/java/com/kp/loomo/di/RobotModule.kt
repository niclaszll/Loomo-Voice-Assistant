package com.kp.loomo.di

import android.content.Context
import com.kp.loomo.features.intents.IntentHandler
import com.kp.loomo.features.robot.RobotManager
import com.kp.loomo.features.robot.SystemSettingsManager
import com.kp.loomo.features.robot.TimerManager
import com.kp.loomo.features.startpage.StartpagePresenter
import com.kp.loomo.features.startpage.TimerViewCallback
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RobotModule {

    @Provides
    @Singleton
    fun provideRobotManager(context: Context): RobotManager {
        return RobotManager(context)
    }

    @Provides
    @Singleton
    fun provideIntentHandler(robotManager: RobotManager, systemSettingsManager: SystemSettingsManager, timeManager: TimerManager): IntentHandler {
        return IntentHandler(robotManager, systemSettingsManager, timeManager)
    }

    @Provides
    @Singleton
    fun provideSystemManager(context: Context): SystemSettingsManager {
        return SystemSettingsManager(context)
    }

    @Provides
    @Singleton
    fun provideTimerManager(context: Context): TimerManager {
        return TimerManager(context)
    }
}