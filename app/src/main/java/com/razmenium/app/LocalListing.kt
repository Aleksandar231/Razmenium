package com.razmenium.app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "listings")
data class LocalListing(
    @PrimaryKey
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val offering: String = "",
    val seeking: String = "",
    val description: String = "",
    val timestamp: Long = 0
)