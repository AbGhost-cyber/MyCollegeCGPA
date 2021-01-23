package com.crushtech.myccga

import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import androidx.appcompat.app.AppCompatActivity
import com.crushtech.myccga.databinding.SplashScreenLayoutBinding
import com.crushtech.myccga.utils.Constants.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setTheme(R.style.splashScreenTheme)
        val binding by viewBinding(SplashScreenLayoutBinding::inflate)
        window.setFlags(
            FLAG_FULLSCREEN,
            FLAG_FULLSCREEN
        )
        setContentView(binding.root)
//        val animation = AnimationUtils.loadAnimation(this, R.anim.tween)
//        binding.apply {
//            appLogo.animation = animation
//        }

        val intent = Intent(this, MainActivity::class.java)
        GlobalScope.launch(Dispatchers.Main) {
            delay(3000L)
            startActivity(intent)
            finish()
        }
    }

}

