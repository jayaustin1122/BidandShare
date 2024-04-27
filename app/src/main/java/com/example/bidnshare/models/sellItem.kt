package com.example.bidnshare.models

data class sellItem(
    val title: String = "",  // Default values for properties
    val price: String = "",
    val imageResourceId: String = "",
    val time: String = "",
    val timeStamp : String =  "",
    val uid : String =  ""
) {
    constructor() : this("", "", "","","","") {
        // Default constructor required for Firebase
    }
}