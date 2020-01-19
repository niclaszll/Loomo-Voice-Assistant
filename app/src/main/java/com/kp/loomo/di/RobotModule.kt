package com.kp.loomo.di

import android.content.Context
import com.kp.loomo.features.intents.IntentHandler
import com.kp.loomo.features.robot.RobotManager
import com.kp.loomo.features.robot.SystemManager
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
    fun provideMoveRobotHandler(robotManager: RobotManager): IntentHandler {
        return IntentHandler(robotManager)
    }

    @Provides
    @Singleton
    fun provideSystemManager(context: Context): SystemManager {
        return SystemManager(context)
    }
}