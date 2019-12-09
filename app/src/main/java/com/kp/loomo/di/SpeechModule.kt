package com.kp.loomo.di

import android.content.Context
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
}