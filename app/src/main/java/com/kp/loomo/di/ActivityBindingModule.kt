package com.kp.loomo.di

import com.kp.loomo.MainActivity
import com.kp.loomo.features.startpage.StartpageModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBindingModule {

    @ActivityScoped
    @ContributesAndroidInjector(modules = [(StartpageModule::class)])
    abstract fun mainActivity(): MainActivity

}