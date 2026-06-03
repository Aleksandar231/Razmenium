package com.razmenium.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnSuccessListener { goToHome() }
                .addOnFailureListener {
                    Toast.makeText(this, "Грешка: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: ApiException) {
            Toast.makeText(this, "Google грешка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

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
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}