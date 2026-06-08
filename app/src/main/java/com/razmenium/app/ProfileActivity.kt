package com.razmenium.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()

        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val tvUserId = findViewById<TextView>(R.id.tvUserId)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        val user = auth.currentUser
        val displayName = user?.displayName
        val email = user?.email
        val name = when {
            displayName != null -> displayName
            email != null -> email
            else -> getString(R.string.anonymous_user)
        }
        tvEmail.text = "${getString(R.string.email_label)}$name"
        tvUserId.text = "${getString(R.string.id_label)}${user?.uid ?: ""}"

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}