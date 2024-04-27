package com.example.bidnshare.user.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bidnshare.models.sellItemAdmin
import com.google.firebase.database.*

class SellItemViewModel : ViewModel() {
    private val sellItemsLiveData = MutableLiveData<List<sellItemAdmin>>()

    // Expose LiveData to observe in the UI
    fun getSellItemsLiveData(): LiveData<List<sellItemAdmin>> {
        return sellItemsLiveData
    }

    fun getAllLiveSellItems() {
        val eventArrayList = ArrayList<sellItemAdmin>()

        val dbRef = FirebaseDatabase.getInstance().getReference("Users")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(usersSnapshot: DataSnapshot) {
                for (userSnapshot in usersSnapshot.children) {
                    val userSellItemsRef = userSnapshot.child("mySellItems")
                    for (sellItemSnapshot in userSellItemsRef.children) {
                        val sellOrLive = sellItemSnapshot.child("sellOrFree").getValue(String::class.java)
                        if (sellOrLive == "Sell" || sellOrLive == "Live") {
                            val title = sellItemSnapshot.child("ItemName").getValue(String::class.java)!!
                            val time = sellItemSnapshot.child("currentTime").getValue(String::class.java)!!
                            val imageUrl = sellItemSnapshot.child("imageURIs").children.firstOrNull()?.getValue(String::class.java)
                            val price = sellItemSnapshot.child("price").getValue(String::class.java)!!
                            val timeStamp = sellItemSnapshot.child("timestamp").getValue(String::class.java)!!
                            val uid = sellItemSnapshot.child("currentUser").getValue(String::class.java)!!
                            val sellOrFree = sellItemSnapshot.child("sellOrFree").getValue(String::class.java)!!
                            val typeSnapshot = sellItemSnapshot.child("type")
                            if (typeSnapshot.exists()) {
                                val type = typeSnapshot.getValue(String::class.java)!!
                                val sellItem = sellItemAdmin(title, price, imageUrl.toString(), time, timeStamp, type, uid,sellOrFree)
                                eventArrayList.add(sellItem)
                            } else {
                                // Handle the case where 'type' field does not exist in the snapshot
                            }

                        }
                    }
                }
                // Update LiveData with the fetched data
                sellItemsLiveData.value = eventArrayList
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event if needed
            }
        })
    }
}
