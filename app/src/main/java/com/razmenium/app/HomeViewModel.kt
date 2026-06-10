package com.razmenium.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Ја држи состојбата на главниот екран: огласи во живо од Firestore,
 * пребарување, омилени и операции врз огласите.
 * Преживува ротации и го гаси Firestore listener-от кога ќе се уништи.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ListingRepository(application)

    private var allListings: List<Listing> = emptyList()
    private var searchQuery: String = ""

    val listings = MutableLiveData<List<Listing>>(emptyList())
    val isLoading = MutableLiveData(true)
    val toastMessage = MutableLiveData<Int?>()

    val favoriteIds: LiveData<Set<String>> =
        repository.favoriteIds().map { it.toSet() }.asLiveData()

    private var registration: ListenerRegistration? = null

    val hasQuery: Boolean
        get() = searchQuery.isNotBlank()

    init {
        registration = repository.listenToListings(
            onChange = { items ->
                allListings = items
                isLoading.value = false
                applyFilter()
            },
            onError = {
                isLoading.value = false
                toastMessage.value = R.string.error_loading_listings
            }
        )
    }

    fun setSearchQuery(query: String) {
        searchQuery = query.trim()
        applyFilter()
    }

    private fun applyFilter() {
        listings.value = if (searchQuery.isBlank()) {
            allListings
        } else {
            allListings.filter {
                it.offering.contains(searchQuery, ignoreCase = true) ||
                        it.seeking.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    fun toggleFavorite(listing: Listing) {
        viewModelScope.launch {
            val added = repository.toggleFavorite(listing)
            toastMessage.value =
                if (added) R.string.saved_to_favorites else R.string.removed_from_favorites
        }
    }

    fun deleteListing(listing: Listing) {
        viewModelScope.launch {
            try {
                repository.deleteListing(listing.id)
                toastMessage.value = R.string.listing_deleted
            } catch (e: Exception) {
                toastMessage.value = R.string.error_generic
            }
        }
    }

    fun onToastShown() {
        toastMessage.value = null
    }

    override fun onCleared() {
        registration?.remove()
        super.onCleared()
    }
}
