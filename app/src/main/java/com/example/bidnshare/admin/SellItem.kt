package com.example.bidnshare.admin

data class SellItem(
    val itemId: String,
    val itemName: String,
    val itemDetails: String,
    val price: String,
    val currentDate: String,
    val currentTime: String,
    val timestamp: String,
    val currentUser: String,
    val sellOrFree: String,
    val type: String,
    val timer: String,
    val imageURIs: List<String>
)
