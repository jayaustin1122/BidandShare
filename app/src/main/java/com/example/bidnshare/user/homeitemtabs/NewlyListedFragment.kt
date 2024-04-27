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
import com.example.bidnshare.adapter.FreeItemAdapter
import com.example.bidnshare.adapter.LiveAuctionAdapter
import com.example.bidnshare.adapter.NewlyListedAdapter
import com.example.bidnshare.databinding.FragmentNewlyListedBinding
import com.example.bidnshare.models.FreeItems
import com.example.bidnshare.models.NewlyItems
import com.example.bidnshare.models.sellItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NewlyListedFragment : Fragment() {
    private lateinit var binding : FragmentNewlyListedBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    // array list to hold events
    private lateinit var newlyArrayList : ArrayList<NewlyItems>
    private lateinit var adapter : NewlyListedAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewlyListedBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this@NewlyListedFragment.requireContext())
        progressDialog.setTitle("PLease wait")
        progressDialog.setCanceledOnTouchOutside(false)
        getAllNewlyListedItems()
    }

    private fun getAllNewlyListedItems() {
        // Initialize the newlyArrayList
        val newlyArrayList = ArrayList<NewlyItems>()

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
                        // Check if type is "Newly"
                        val type = sellItemSnapshot.child("type").getValue(String::class.java)
                        if (type == "Newly") {
                            // Retrieve data from the snapshot and create a NewlyItems object
                            val title = sellItemSnapshot.child("ItemName").getValue(String::class.java)!!
                            val price = sellItemSnapshot.child("price").getValue(String::class.java)!!
                            val imageUrl = sellItemSnapshot.child("imageURIs").children.firstOrNull()?.getValue(String::class.java)
                            val newlyItem = NewlyItems(title, price, imageUrl.toString())
                            newlyArrayList.add(newlyItem)
                        }
                    }
                }

                // Set up the adapter after retrieving data for all users
                lifecycleScope.launchWhenResumed {
                    adapter = NewlyListedAdapter(this@NewlyListedFragment.requireContext(), newlyArrayList)
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