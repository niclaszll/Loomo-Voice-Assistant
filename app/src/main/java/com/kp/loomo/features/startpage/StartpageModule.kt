package com.kp.loomo.features.startpage

import com.kp.loomo.di.ActivityScoped
import com.kp.loomo.di.FragmentScoped
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Used for dependency injection (see "di" folder)
 */
@Module
abstract class StartpageModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun startpageFragment(): StartpageFragment

    @ActivityScoped
    @Binds
    abstract fun startpagePresenter(presenter: StartpagePresenter): StartpageContract.Presenter
}