package com.example.bidnshare.user.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.bidnshare.R
import com.example.bidnshare.databinding.FragmentPendingTransactionsBinding
import com.example.bidnshare.models.sellItemAdmin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class PendingTransactionsFragment : Fragment() {
    private lateinit var binding : FragmentPendingTransactionsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPendingTransactionsBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        getAllLiveSellItems()
    }
    fun getAllLiveSellItems() {

        val dbRef = FirebaseDatabase.getInstance().getReference("Users")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(usersSnapshot: DataSnapshot) {
                for (userSnapshot in usersSnapshot.children) {
                    val userSellItemsRef = userSnapshot.child("mySellItems")
                    for (sellItemSnapshot in userSellItemsRef.children) {
                        val orderedBy = sellItemSnapshot.child("orderedBy").getValue(String::class.java)
                        if (orderedBy == auth.uid ) {
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
                                Toast.makeText(context, title, Toast.LENGTH_SHORT).show()
                            } else {
                                // Handle the case where 'type' field does not exist in the snapshot
                            }

                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event if needed
            }
        })
    }
}