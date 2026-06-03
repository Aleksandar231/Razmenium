package com.razmenium.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            goToHome()
            return
        }

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnGoogle = findViewById<Button>(R.id.btnGoogle)
        val btnAnonymous = findViewById<Button>(R.id.btnAnonymous)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Внеси е-пошта и лозинка", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { goToHome() }
                .addOnFailureListener {
                    Toast.makeText(this, "Грешка: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Внеси е-пошта и лозинка", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { goToHome() }
                .addOnFailureListener {
                    Toast.makeText(this, "Грешка: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        btnAnonymous.setOnClickListener {
            auth.signInAnonymously()
                .addOnSuccessListener { goToHome() }
                .addOnFailureListener {
                    Toast.makeText(this, "Грешка: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        btnGoogle.setOnClickListener {
            Toast.makeText(this, "Google логирање - наскоро!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}