package com.example.bidnshare.user.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.bidnshare.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar
import java.util.TimeZone

class BidViewModel : ViewModel() {
    private lateinit var auth: FirebaseAuth
    private val _uploadStatus = MutableLiveData<Boolean>()
    val uploadStatus: LiveData<Boolean>
        get() = _uploadStatus
    init {

        auth = FirebaseAuth.getInstance()
    }
    private fun getCurrentTime(): String {
        val tz = TimeZone.getTimeZone("GMT+08:00")
        val c = Calendar.getInstance(tz)
        val hours = String.format("%02d", c.get(Calendar.HOUR))
        val minutes = String.format("%02d", c.get(Calendar.MINUTE))
        return "$hours:$minutes"
    }

    fun uploadBidPrice(bidPrice: Double, timeStamp: String,uidUser:String) {
        val databaseRef = FirebaseDatabase.getInstance().reference
        databaseRef.child("Users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(usersSnapshot: DataSnapshot) {
                    // Iterate through each user
                    for (userSnapshot in usersSnapshot.children) {
                        val uid = userSnapshot.key
                        // Get the reference to the user's "mySellItems" node
                        val mySellItemsRef = userSnapshot.child("mySellItems")
                        // Check if the user has an item with the provided timestamp
                        var foundItemSnapshot: DataSnapshot? = null
                        for (itemSnapshot in mySellItemsRef.children) {
                            val itemTimeStamp =
                                itemSnapshot.child("timestamp").getValue(String::class.java)
                            if (itemTimeStamp == timeStamp) {
                                foundItemSnapshot = itemSnapshot
                                break
                            }
                        }
                        foundItemSnapshot?.let { itemSnapshot ->
                            val bidPathRef = databaseRef.child("Users").child(uidUser).child("mySellItems").child(timeStamp).child("Bids")
                            val timestamp2 = System.currentTimeMillis()
                            // Create a new child node under the bid path to save the bid information
                            val newBidRef = bidPathRef.push()
                            val bidData = HashMap<String, Any>()
                            bidData["bidPrice"] = bidPrice.toString()
                            bidData["userBid"] = auth.uid.toString()
                            bidData["timestamp"] = timestamp2.toString()
                            bidData["bidStatus"] = false

                            newBidRef.setValue(bidData)
                                .addOnSuccessListener {
                                    _uploadStatus.value = true
                                }
                                .addOnFailureListener { exception ->
                                    // Handle failure
                                    Log.e("BidUploadError", "Error uploading bid price: ${exception.message}")
                                    _uploadStatus.value = false
                                }
                        }
                        // Break the loop since we found the user with the item
                        break
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled event if needed
                    _uploadStatus.value = false
                }
            })

    }
}