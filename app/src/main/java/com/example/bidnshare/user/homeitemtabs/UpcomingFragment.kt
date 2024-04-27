package com.example.bidnshare.user.homeitemtabs

import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bidnshare.R
import com.example.bidnshare.adapter.NewlyListedAdapter
import com.example.bidnshare.adapter.UpcomingAdapter
import com.example.bidnshare.databinding.FragmentUpcomingBinding
import com.example.bidnshare.models.NewlyItems
import com.example.bidnshare.models.UpcomingItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class UpcomingFragment : Fragment() {
    private lateinit var binding : FragmentUpcomingBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    // array list to hold events
    private lateinit var upcomingArrayList : ArrayList<UpcomingItems>
    private lateinit var adapter : UpcomingAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUpcomingBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this@UpcomingFragment.requireContext())
        progressDialog.setTitle("PLease wait")
        progressDialog.setCanceledOnTouchOutside(false)
        getAllUpcomingItems()
    }
    private fun getAllUpcomingItems() {
        // Initialize the upcomingArrayList
        val upcomingArrayList = ArrayList<UpcomingItems>()

        // Get a reference to the Firebase database
        val dbRef = FirebaseDatabase.getInstance().getReference("Users")

        // Add a ValueEventListener to retrieve data
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(usersSnapshot: DataSnapshot) {
                // Iterate through each user
                for (userSnapshot in usersSnapshot.children) {
                    // Get a reference to the sellItems for the current user
                    val userSellItemsRef = userSnapshot.child("mySellItems")

                    // Iterate through each sell item for the current user
                    for (sellItemSnapshot in userSellItemsRef.children) {
                        // Check if type is "Upcoming"
                        val type = sellItemSnapshot.child("type").getValue(String::class.java)
                        if (type == "Upcoming") {
                            // Retrieve data from the snapshot and create an UpcomingItems object
                            val title = sellItemSnapshot.child("ItemName").getValue(String::class.java)!!
                            val price = sellItemSnapshot.child("price").getValue(String::class.java)!!
                            val imageUrl = sellItemSnapshot.child("imageURIs").children.firstOrNull()?.getValue(String::class.java)
                            val upcomingItem = UpcomingItems(title, price, imageUrl.toString())
                            upcomingArrayList.add(upcomingItem)
                        }
                    }
                }

                // Set up the adapter after retrieving data for all users
                lifecycleScope.launchWhenResumed {
                    adapter = UpcomingAdapter(this@UpcomingFragment.requireContext(), upcomingArrayList)
                    binding.recycler.setHasFixedSize(true)
                    val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    binding.recycler.layoutManager = layoutManager
                    binding.recycler.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event if needed
            }
        })
    }


}