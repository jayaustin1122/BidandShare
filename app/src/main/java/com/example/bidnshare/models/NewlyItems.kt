package com.example.bidnshare.models

data class NewlyItems (
    val title: String = "",  // Default values for properties
    val price: String? = null,
    val imageResourceId: String = ""
) {
    constructor() : this("", null, "") {
        // Default constructor required for Firebase
    }
}