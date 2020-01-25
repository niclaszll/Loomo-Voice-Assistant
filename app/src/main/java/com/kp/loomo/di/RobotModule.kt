package com.kp.loomo.di

import android.content.Context
import com.kp.loomo.features.intents.IntentHandler
import com.kp.loomo.features.robot.RobotManager
import com.kp.loomo.features.robot.SystemSettingsManager
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
    fun provideIntentHandler(robotManager: RobotManager, systemSettingsManager: SystemSettingsManager): IntentHandler {
        return IntentHandler(robotManager, systemSettingsManager)
    }

    @Provides
    @Singleton
    fun provideSystemManager(context: Context): SystemSettingsManager {
        return SystemSettingsManager(context)
    }
}