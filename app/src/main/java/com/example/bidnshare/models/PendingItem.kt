package com.example.bidnshare.models
data class PendingItem(
    val title: String,
    val time: String,
    val imageUrl: String?,
    val price: String,
    val timeStamp: String,
    val uid: String,
    val sellOrFree: String,
    val type: String
)
