package com.kp.loomo.features.startpage

import com.kp.BasePresenter
import com.kp.BaseView

interface StartpageContract {

    interface View : BaseView<Presenter> {
        fun showText(resourceIdentifier: Int)
        fun showText(text: String)
    }

    interface Presenter : BasePresenter<View> {

        override fun takeView(view: View)
        override fun dropView()
        fun startSnips()
        fun initSpeech()
    }
}