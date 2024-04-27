package com.example.bidnshare.models

data class sellItemAdmin(
    val title: String = "",  // Default values for properties
    val price: String = "",
    val imageResourceId: String = "",
    val time: String = "",
    val timeStamp : String =  "",
    val type : String =  "",
    val uid : String =  "",
    val sellOrFree: String = ""
) {
    constructor() : this("", "", "","","","","","") {
        // Default constructor required for Firebase
    }
}