package com.thn.videoconstruction.fe_ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.thn.videoconstruction.R
import com.thn.videoconstruction.fe_ui.HomeActivityFE
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {
    private var i: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash)
        setProgessbar()
    }

    fun setProgessbar() {
        Handler(Looper.getMainLooper()).postDelayed({
            i += 20
            progress_circular.progress = i
            if (i == 100) {
                openHomeScreen()
            }
            setProgessbar()
        }, 200)
    }

    private fun openHomeScreen() {
        val intent = Intent(this, HomeActivityFE::class.java)
        startActivity(intent)
        finish()
    }

}


