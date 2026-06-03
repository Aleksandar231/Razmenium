package com.razmenium.app

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddListingActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_listing)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val etOffering = findViewById<TextInputEditText>(R.id.etOffering)
        val etSeeking = findViewById<TextInputEditText>(R.id.etSeeking)
        val etDescription = findViewById<TextInputEditText>(R.id.etDescription)
        val btnPublish = findViewById<Button>(R.id.btnPublish)

        btnPublish.setOnClickListener {
            val offering = etOffering.text.toString().trim()
            val seeking = etSeeking.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (offering.isEmpty() || seeking.isEmpty()) {
                Toast.makeText(this, "Внеси што нудиш и што бараш", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = auth.currentUser
            val listing = hashMapOf(
                "userId" to (user?.uid ?: ""),
                "userName" to (user?.email ?: "Анонимен"),
                "offering" to offering,
                "seeking" to seeking,
                "description" to description,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("listings")
                .add(listing)
                .addOnSuccessListener {
                    Toast.makeText(this, "Огласот е објавен!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Грешка: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}