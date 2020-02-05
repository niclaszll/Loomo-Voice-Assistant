package com.kp.loomo.features.startpage

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.kp.loomo.R
import com.kp.loomo.commons.extensions.inflate
import com.kp.loomo.di.ActivityScoped
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_startpage.*
import javax.inject.Inject

/**
 * Startpage fragment, that currently renders everything needed
 */
@ActivityScoped
class StartpageFragment @Inject constructor(private var applicationContext: Context) :
    DaggerFragment(), StartpageContract.View {

    @Inject
    lateinit var presenter: StartpageContract.Presenter

    /**
     * Called when StartpageFragment is created
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // check if voice recording permissions are granted
        if (ensurePermissions()) {
            Log.d("StartpageFragment", "Permissions granted")
            presenter.initSpeech()
        } else {
            Log.d("StartpageFragment", "Permissions not granted")
        }

        return container?.inflate(R.layout.fragment_startpage)
    }

    /**
     * Called immediately after onCreateView
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_manual.setOnClickListener {
            presenter.initManualSpeech()
        }
    }

    /**
     * Render text from resourceIdentifier
     */
    override fun showText(resourceIdentifier: Int) {
        textView.text = getString(resourceIdentifier)
    }

    /**
     * Render text from String
     */
    override fun showText(text: String) {
        textView.text = text
    }

    /**
     * Ensure permissions are granted
     */
    private fun ensurePermissions(): Boolean {
        val status = ActivityCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.RECORD_AUDIO
        )
        if (status != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), 0
            )

            return false
        }
        return true
    }

    /**
     * Callback for permissions result
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("StartpageFragment", "Permissions granted 2")
            presenter.initSpeech()
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.takeView(this)
    }

    override fun onDestroy() {
        presenter.dropView()
        super.onDestroy()
    }

}