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