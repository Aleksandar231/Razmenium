package com.razmenium.app

data class Listing(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val offering: String = "",
    val seeking: String = "",
    val description: String = "",
    val timestamp: Long = 0
)

fun Listing.toLocal() = LocalListing(
    id = id,
    userId = userId,
    userName = userName,
    offering = offering,
    seeking = seeking,
    description = description,
    timestamp = timestamp
)

fun LocalListing.toListing() = Listing(
    id = id,
    userId = userId,
    userName = userName,
    offering = offering,
    seeking = seeking,
    description = description,
    timestamp = timestamp
)
