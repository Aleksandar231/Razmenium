package com.razmenium.app

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private var allListings = listOf<Listing>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val lang = prefs.getString("language", "mk") ?: "mk"
        val locale = java.util.Locale(lang)
        java.util.Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.rvListings)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterListings(newText ?: "")
                return true
            }
        })

        val searchEditText = searchView.findViewById<android.widget.EditText>(
            androidx.appcompat.R.id.search_src_text
        )

        val isDarkMode = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        if (isDarkMode) {
            searchEditText.setTextColor(resources.getColor(android.R.color.white, theme))
            searchEditText.setHintTextColor(resources.getColor(android.R.color.darker_gray, theme))
        } else {
            searchEditText.setTextColor(resources.getColor(android.R.color.black, theme))
            searchEditText.setHintTextColor(resources.getColor(android.R.color.darker_gray, theme))
        }

        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)
        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddListingActivity::class.java))
        }

        loadListings()
    }

    private fun filterListings(query: String) {
        val filtered = if (query.isEmpty()) {
            allListings
        } else {
            allListings.filter {
                it.offering.contains(query, ignoreCase = true) ||
                        it.seeking.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }
        recyclerView.adapter = ListingAdapter(
            listings = filtered,
            onFavoriteClick = { listing -> saveFavorite(listing) },
            onDeleteClick = { listing -> deleteListing(listing) }
        )
    }

    private fun saveFavorite(listing: Listing) {
        Thread {
            val localListing = LocalListing(
                id = listing.id,
                userId = listing.userId,
                userName = listing.userName,
                offering = listing.offering,
                seeking = listing.seeking,
                description = listing.description,
                timestamp = listing.timestamp
            )
            AppDatabase.getDatabase(this).listingDao().insertListing(localListing)
            runOnUiThread {
                Toast.makeText(this, "Зачувано во омилени!", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    private fun deleteListing(listing: Listing) {
        db.collection("listings").document(listing.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Огласот е избришан!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Грешка: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadListings() {
        db.collection("listings")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                allListings = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Listing::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                recyclerView.adapter = ListingAdapter(
                    listings = allListings,
                    onFavoriteClick = { listing -> saveFavorite(listing) },
                    onDeleteClick = { listing -> deleteListing(listing) }
                )
            }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                true
            }
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            R.id.action_language -> {
                val currentLang = resources.configuration.locales[0].language
                val newLang = if (currentLang == "en") "mk" else "en"
                val prefs = getSharedPreferences("settings", MODE_PRIVATE)
                prefs.edit().putString("language", newLang).apply()
                val newLocale = java.util.Locale(newLang)
                java.util.Locale.setDefault(newLocale)
                val config = resources.configuration
                config.setLocale(newLocale)
                resources.updateConfiguration(config, resources.displayMetrics)
                recreate()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}