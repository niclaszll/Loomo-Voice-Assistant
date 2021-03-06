package com.kp.loomo.di

import android.content.Context
import android.content.SharedPreferences
import com.kp.loomo.features.speech.DialogflowManager
import com.kp.loomo.features.speech.GoogleCloudTTSManager
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
    fun provideDialogflow(context: Context): DialogflowManager {
        return DialogflowManager(context)
    }

    @Provides
    @Singleton
    fun provideGoogleCloudTTS(sharedPreferences: SharedPreferences): GoogleCloudTTSManager {
        return GoogleCloudTTSManager(sharedPreferences)
    }
}