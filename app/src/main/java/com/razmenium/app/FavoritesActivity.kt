package com.razmenium.app

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.razmenium.app.databinding.ActivityFavoritesBinding

/**
 * Омилени огласи зачувани локално (Room) — работи и офлајн.
 */
class FavoritesActivity : BaseActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var adapter: ListingAdapter

    private val viewModel: FavoritesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = ListingAdapter(
            onFavoriteClick = { listing ->
                viewModel.remove(listing)
                Toast.makeText(this, R.string.removed_from_favorites, Toast.LENGTH_SHORT).show()
            },
            showOwnerActions = false
        )
        binding.rvFavorites.layoutManager = LinearLayoutManager(this)
        binding.rvFavorites.adapter = adapter

        viewModel.favorites.observe(this) { items ->
            adapter.favoriteIds = items.map { it.id }.toSet()
            adapter.submitList(items)
            binding.emptyState.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
