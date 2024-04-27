package com.example.bidnshare.ui.signup

import android.net.Uri
import androidx.lifecycle.ViewModel

class SignUpViewModel  : ViewModel() {
    var fullname: String = ""
    var email: String = ""
    var password: String = ""
    var month : String = ""
    var day : String = ""
    var year : String = ""
    var gender : String = ""
    var image: Uri? = null // Add this line to store the selected image URI

    // Add a method to set the image URI
    fun setImage2(uri: Uri?) {
        image = uri
    }
}