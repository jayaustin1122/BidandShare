package com.example.bidnshare.models

data class BidModels (
    val name: String = "",
    val bidPrice: String = "",
    val imageUrl: String= "",
    val minsAgo: String = "", // Change to Long
) {
    constructor() : this("", "", "","") {
        // Default constructor required for Firebase
    }
}
