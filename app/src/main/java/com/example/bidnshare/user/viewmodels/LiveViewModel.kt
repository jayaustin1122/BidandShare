package com.example.bidnshare.user.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bidnshare.models.sellItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LiveViewModel : ViewModel() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val sellItemsRef by lazy {
        FirebaseDatabase.getInstance().getReference("Users")
            .child(auth.currentUser?.uid ?: "")
            .child("mySellItems")
    }
    private val _sellItems = MutableLiveData<List<sellItem>>()
    val sellItems: LiveData<List<sellItem>> = _sellItems

    init {
        fetchSellItems()
    }

    private fun fetchSellItems() {
        sellItemsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<sellItem>()
                for (itemSnapshot in snapshot.children) {
                    val title = itemSnapshot.child("title").getValue(String::class.java)
                    val price = itemSnapshot.child("price").getValue(Long::class.java)?.toString()
                    val imageUrl = itemSnapshot.child("imageURIs").child("0").getValue(String::class.java) // Retrieve the first image URL
                    if (title != null && price != null && imageUrl != null) {
                        items.add(sellItem(title, price, imageUrl)) // Pass imageUrl to sellItem constructor
                    }
                }
                _sellItems.value = items
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}