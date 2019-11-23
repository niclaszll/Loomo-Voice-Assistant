package com.kp.loomo

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
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

    /**
     * Called when MainActivity is created
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        // detach-attach to refresh if already in searchfragment
        // ft.detach(fragment)
        ft.attach(fragment)
        ft.addToBackStack(null)
        ft.commit()
        Log.v("MainActivity", "Fragment changed")
    }
}
