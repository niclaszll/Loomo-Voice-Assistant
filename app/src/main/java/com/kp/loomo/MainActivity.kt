package com.kp.loomo

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.kp.loomo.features.robot.SystemSettingsManager
import com.kp.loomo.features.startpage.StartpageFragment
import dagger.Lazy
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject


/**
 * Main activity that contains all future fragments (child views)
 */
class MainActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var startpageFragmentProvider: Lazy<StartpageFragment>

    @Inject
    lateinit var systemManager: SystemSettingsManager

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    /**
     * Called when MainActivity is created
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        systemManager.setupSystemManager(window)

        if (savedInstanceState == null) {
            @Suppress("UNCHECKED_CAST")
            changeToFragment(startpageFragmentProvider as Lazy<Fragment>)
        }
    }


    /**
     * Finish activity when reaching the last fragment.
     */
    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount > 1) {
            fragmentManager.popBackStackImmediate()
        } else {
            finish()
        }
    }

    /**
     * Switch between fragments
     */
    @SuppressLint("PrivateResource")
    private fun changeToFragment(f: Lazy<Fragment>) {
        val fragment = f.get()
        val ft = supportFragmentManager.beginTransaction()

        ft.setCustomAnimations(
            R.anim.abc_fade_in,
            R.anim.abc_fade_out,
            R.anim.abc_popup_enter,
            R.anim.abc_popup_exit
        )

        ft.replace(R.id.activity_base_content, fragment)
        ft.attach(fragment)
        ft.addToBackStack(null)
        ft.commit()
        Log.v("MainActivity", "Fragment changed")
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    /**
     * Menu actions to switch between TTS method and voice gender (for online speech)
     */
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        R.id.action_google_tts -> {
            val editor = sharedPrefs.edit()
            val enableGoogleCloudTTS = sharedPrefs.getBoolean("google_tts", false)

            if (enableGoogleCloudTTS) {
                editor.putBoolean("google_tts", false)
                editor.apply()
                val toast = Toast.makeText(
                    applicationContext,
                    "Google Cloud TTS deactivated",
                    Toast.LENGTH_SHORT
                )
                toast.show()
            } else {
                editor.putBoolean("google_tts", true)
                editor.apply()
                val toast = Toast.makeText(
                    applicationContext,
                    "Google Cloud TTS activated",
                    Toast.LENGTH_SHORT
                )
                toast.show()
            }

            true
        }
        R.id.action_voice_gender -> {
            val editor = sharedPrefs.edit()
            val voiceGender = sharedPrefs.getString("voice_gender", "FEMALE")

            if (voiceGender == "FEMALE") {
                editor.putString("voice_gender", "MALE")
                editor.apply()
                val toast = Toast.makeText(
                    applicationContext,
                    "Cloud voice gender changed to male.",
                    Toast.LENGTH_SHORT
                )
                toast.show()
            } else {
                editor.putString("voice_gender", "FEMALE")
                editor.apply()
                val toast = Toast.makeText(
                    applicationContext,
                    "Cloud voice gender changed to female.",
                    Toast.LENGTH_SHORT
                )
                toast.show()
            }

            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }



}
