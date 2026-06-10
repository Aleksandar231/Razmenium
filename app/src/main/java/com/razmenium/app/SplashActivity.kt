package com.razmenium.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : BaseActivity() {

    private val handler = Handler(Looper.getMainLooper())

    private val navigate = Runnable {
        if (isFinishing || isDestroyed) return@Runnable
        // Логиран корисник оди директно на огласите
        val next = if (FirebaseAuth.getInstance().currentUser != null) {
            HomeActivity::class.java
        } else {
            MainActivity::class.java
        }
        startActivity(Intent(this, next))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        handler.postDelayed(navigate, SPLASH_DELAY_MS)
    }

    override fun onDestroy() {
        handler.removeCallbacks(navigate)
        super.onDestroy()
    }

    companion object {
        private const val SPLASH_DELAY_MS = 1500L
    }
}
