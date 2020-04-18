package com.kp.loomo.di

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import com.kp.loomo.features.intents.IntentHandler
import com.kp.loomo.features.robot.MediaManager
import com.kp.loomo.features.robot.RobotManager
import com.kp.loomo.features.robot.SystemSettingsManager
import com.kp.loomo.features.robot.TimerManager
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
    fun provideIntentHandler(robotManager: RobotManager, systemSettingsManager: SystemSettingsManager, timeManager: TimerManager, connectivityManager: ConnectivityManager, sharedPreferences: SharedPreferences, mediaManager: MediaManager): IntentHandler {
        return IntentHandler(robotManager, systemSettingsManager, timeManager, connectivityManager, sharedPreferences, mediaManager)
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

    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(
            "SHARED_PREFERENCES", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun mediaManager(context: Context): MediaManager {
        return MediaManager(context)
    }
}