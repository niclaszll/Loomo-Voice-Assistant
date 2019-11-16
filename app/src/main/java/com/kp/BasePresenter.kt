package com.kp

interface BasePresenter<T> {

    fun takeView(view: T)

    fun dropView()

}