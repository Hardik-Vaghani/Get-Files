package com.hardik.getfiles

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.hardik.getfiles.ui.FilesFragment
import com.hardik.getfiles.R
import com.hardik.getfiles.common.FragmentSessionUtils

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName

    val fragmentSessionUtils = FragmentSessionUtils.getInstance()
    val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // load the default Fragment with data
        if (savedInstanceState == null) {
            switchToSplashScreenFragment()
        }
    }

    private fun switchToSplashScreenFragment() {
        Log.d(TAG, "switchToNotepadFragment: ")

        Handler(Looper.getMainLooper()).run {
            postDelayed({
                if (currentFragment !is FilesFragment) {
                    fragmentSessionUtils.switchFragment(
                        supportFragmentManager,
                        FilesFragment(),
                        false,
                    )
                }
            }, 0)
        }
    }
}