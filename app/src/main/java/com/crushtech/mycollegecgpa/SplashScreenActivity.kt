package com.crushtech.mycollegecgpa

import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.splash_screen_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setTheme(R.style.splashScreenTheme)
        window.setFlags(
            FLAG_FULLSCREEN,
            FLAG_FULLSCREEN
        )
        setContentView(R.layout.splash_screen_layout)

        AnimationUtils.loadAnimation(this, android.R.anim.fade_in).apply {
            appLogo.startAnimation(this)
            appNameTv.startAnimation(this)
        }


        val intent = Intent(this, MainActivity::class.java)
        GlobalScope.launch(Dispatchers.Main) {
            delay(3000L)
            startActivity(intent)
            finish()
        }
    }
}