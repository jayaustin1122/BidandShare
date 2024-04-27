package com.example.bidnshare.util

import android.app.ProgressDialog
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class Inits(private val context: Context) {
    private lateinit var progressDialog: ProgressDialog
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var database: FirebaseDatabase

    fun initialize() {
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance()

        progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
    }

    fun getAuth(): FirebaseAuth {
        return auth
    }

    fun getStorage(): FirebaseStorage {
        return storage
    }

    fun getDatabase(): FirebaseDatabase {
        return database
    }

    fun getProgressDialog(): ProgressDialog {
        return progressDialog
    }
}
