package com.razmenium.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            // Логиран корисник оди директно на огласите
            val next = if (FirebaseAuth.getInstance().currentUser != null) {
                HomeActivity::class.java
            } else {
                MainActivity::class.java
            }
            startActivity(Intent(this, next))
            finish()
        }, SPLASH_DELAY_MS)
    }

    companion object {
        private const val SPLASH_DELAY_MS = 1500L
    }
}
