package com.razmenium.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.razmenium.app.databinding.ActivityHomeBinding

class HomeActivity : BaseActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: ListingAdapter

    private val viewModel: HomeViewModel by viewModels()

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        askNotificationPermission()

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
                when {
                    viewModel.loadFailed -> R.string.error_loading_listings
                    viewModel.hasQuery -> R.string.no_search_results
                    else -> R.string.no_listings
                }
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
                logoutAndExit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleDarkMode() {
        // Се проверува моменталниот изглед (не зачуваниот режим) — така toggle-от
        // работи правилно и кога темата следи систем кој е веќе темен
        val isNightNow = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        val newMode = if (isNightNow) {
            AppCompatDelegate.MODE_NIGHT_NO
        } else {
            AppCompatDelegate.MODE_NIGHT_YES
        }
        getSharedPreferences(RazmeniumApp.PREFS_NAME, MODE_PRIVATE)
            .edit().putInt(RazmeniumApp.KEY_NIGHT_MODE, newMode).apply()
        AppCompatDelegate.setDefaultNightMode(newMode)
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
