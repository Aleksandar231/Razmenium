package com.razmenium.app

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

/**
 * Единствена точка за пристап до податоците:
 * Firestore (огласи) + Room (омилени, достапни офлајн).
 */
class ListingRepository(context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val dao = AppDatabase.getDatabase(context).listingDao()

    fun listenToListings(
        onChange: (List<Listing>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return db.collection(COLLECTION_LISTINGS)
            .orderBy(FIELD_TIMESTAMP, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Listing::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                onChange(items)
            }
    }

    suspend fun addListing(listing: Map<String, Any>) {
        db.collection(COLLECTION_LISTINGS).add(listing).await()
    }

    suspend fun updateListing(id: String, fields: Map<String, Any>) {
        db.collection(COLLECTION_LISTINGS).document(id).update(fields).await()
    }

    suspend fun deleteListing(id: String) {
        db.collection(COLLECTION_LISTINGS).document(id).delete().await()
        // Ако огласот бил зачуван во омилени, исчисти го и локално
        dao.deleteListing(id)
    }

    suspend fun countUserListings(userId: String): Int =
        db.collection(COLLECTION_LISTINGS)
            .whereEqualTo(FIELD_USER_ID, userId)
            .get().await().size()

    fun favorites(): Flow<List<LocalListing>> = dao.getAllListings()

    fun favoriteIds(): Flow<List<String>> = dao.getAllIds()

    suspend fun favoritesCount(): Int = dao.count()

    suspend fun removeFavorite(id: String) = dao.deleteListing(id)

    /** Враќа true ако огласот е додаден во омилени, false ако е отстранет. */
    suspend fun toggleFavorite(listing: Listing): Boolean {
        return if (dao.isFavorite(listing.id)) {
            dao.deleteListing(listing.id)
            false
        } else {
            dao.insertListing(listing.toLocal())
            true
        }
    }

    companion object {
        const val COLLECTION_LISTINGS = "listings"
        const val FIELD_TIMESTAMP = "timestamp"
        const val FIELD_USER_ID = "userId"
        const val FIELD_USER_NAME = "userName"
        const val FIELD_OFFERING = "offering"
        const val FIELD_SEEKING = "seeking"
        const val FIELD_DESCRIPTION = "description"
    }
}
