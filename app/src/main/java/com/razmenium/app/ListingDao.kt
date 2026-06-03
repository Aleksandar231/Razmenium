package com.razmenium.app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ListingDao {

    @Query("SELECT * FROM listings ORDER BY timestamp DESC")
    fun getAllListings(): List<LocalListing>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertListing(listing: LocalListing)

    @Query("DELETE FROM listings WHERE id = :id")
    fun deleteListing(id: String)

    @Query("DELETE FROM listings")
    fun deleteAll()
}