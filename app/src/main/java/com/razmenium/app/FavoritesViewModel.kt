package com.razmenium.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Омилени огласи од локалната Room база — достапни и без интернет.
 */
class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ListingRepository(application)

    val favorites: LiveData<List<Listing>> =
        repository.favorites().map { list -> list.map { it.toListing() } }.asLiveData()

    fun remove(listing: Listing) {
        viewModelScope.launch {
            try {
                repository.removeFavorite(listing.id)
            } catch (_: Exception) {
                // ретка грешка во локалната база — листата останува непроменета
            }
        }
    }
}
