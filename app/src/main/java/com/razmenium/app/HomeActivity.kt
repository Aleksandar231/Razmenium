package com.razmenium.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.login.LoginManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.razmenium.app.databinding.ActivityHomeBinding

class HomeActivity : BaseActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ListingAdapter

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setSupportActionBar(binding.toolbar)

        adapter = ListingAdapter(
            onFavoriteClick = { listing -> viewModel.toggleFavorite(listing) },
            onEditClick = { listing -> openEdit(listing) },
            onDeleteClick = { listing -> confirmDelete(listing) }
        )
        binding.rvListings.layoutManager = LinearLayoutManager(this)
        binding.rvListings.adapter = adapter

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddListingActivity::class.java))
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.listings.observe(this) { items ->
            adapter.submitList(items)
            val showEmpty = items.isEmpty() && viewModel.isLoading.value != true
            binding.emptyState.visibility = if (showEmpty) View.VISIBLE else View.GONE
            binding.tvEmptyText.text = getString(
                if (viewModel.hasQuery) R.string.no_search_results else R.string.no_listings
            )
        }

        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.favoriteIds.observe(this) { ids ->
            adapter.favoriteIds = ids
        }

        viewModel.toastMessage.observe(this) { resId ->
            if (resId != null) {
                Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
                viewModel.onToastShown()
            }
        }
    }

    private fun openEdit(listing: Listing) {
        val intent = Intent(this, AddListingActivity::class.java).apply {
            putExtra(AddListingActivity.EXTRA_ID, listing.id)
            putExtra(AddListingActivity.EXTRA_OFFERING, listing.offering)
            putExtra(AddListingActivity.EXTRA_SEEKING, listing.seeking)
            putExtra(AddListingActivity.EXTRA_DESCRIPTION, listing.description)
        }
        startActivity(intent)
    }

    private fun confirmDelete(listing: Listing) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_listing_title)
            .setMessage(R.string.delete_listing_message)
            .setPositiveButton(R.string.delete) { _, _ -> viewModel.deleteListing(listing) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_favorites -> {
                startActivity(Intent(this, FavoritesActivity::class.java))
                true
            }
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            R.id.action_dark_mode -> {
                toggleDarkMode()
                true
            }
            R.id.action_language -> {
                val newLang = if (LocaleHelper.getLanguage(this) == "en") "mk" else "en"
                LocaleHelper.setLanguage(this, newLang)
                recreate()
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleDarkMode() {
        val prefs = getSharedPreferences(RazmeniumApp.PREFS_NAME, MODE_PRIVATE)
        val current = prefs.getInt(
            RazmeniumApp.KEY_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )
        val newMode = if (current == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.MODE_NIGHT_NO
        } else {
            AppCompatDelegate.MODE_NIGHT_YES
        }
        prefs.edit().putInt(RazmeniumApp.KEY_NIGHT_MODE, newMode).apply()
        AppCompatDelegate.setDefaultNightMode(newMode)
    }

    private fun logout() {
        auth.signOut()
        LoginManager.getInstance().logOut()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
