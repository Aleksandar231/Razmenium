package com.razmenium.app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {

    @Query("SELECT * FROM listings ORDER BY timestamp DESC")
    fun getAllListings(): Flow<List<LocalListing>>

    @Query("SELECT id FROM listings")
    fun getAllIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM listings WHERE id = :id)")
    suspend fun isFavorite(id: String): Boolean

    @Query("SELECT COUNT(*) FROM listings")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: LocalListing)

    @Query("UPDATE listings SET offering = :offering, seeking = :seeking, description = :description WHERE id = :id")
    suspend fun updateListing(id: String, offering: String, seeking: String, description: String)

    @Query("DELETE FROM listings WHERE id = :id")
    suspend fun deleteListing(id: String)
}
