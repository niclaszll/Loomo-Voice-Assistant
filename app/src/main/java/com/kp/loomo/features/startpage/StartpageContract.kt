package com.kp.loomo.features.startpage

import com.kp.BasePresenter
import com.kp.BaseView

interface StartpageContract {

    interface View : BaseView<Presenter> {
        fun showText(resourceIdentifier: Int, view: OutputView)
        fun showText(text: String, view: OutputView)
    }

    interface Presenter : BasePresenter<View> {

        override fun takeView(view: View)
        override fun dropView()
        fun initSpeech()
        fun initManualSpeech()
    }
}