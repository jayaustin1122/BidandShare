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
import com.example.bidnshare.adapter.EndingAdapter
import com.example.bidnshare.adapter.NewlyListedAdapter
import com.example.bidnshare.adapter.UpcomingAdapter
import com.example.bidnshare.databinding.FragmentEndingSoonBinding
import com.example.bidnshare.models.EndingItems
import com.example.bidnshare.models.NewlyItems
import com.example.bidnshare.models.UpcomingItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class EndingSoonFragment : Fragment() {
    private lateinit var binding : FragmentEndingSoonBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    // array list to hold events
    private lateinit var endingArrayList : ArrayList<EndingItems>
    private lateinit var adapter : EndingAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEndingSoonBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this@EndingSoonFragment.requireContext())
        progressDialog.setTitle("PLease wait")
        progressDialog.setCanceledOnTouchOutside(false)
        getAllEndingItems()
    }

    private fun getAllEndingItems() {
        // Initialize the endingArrayList
        val endingArrayList = ArrayList<EndingItems>()

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
                        // Check if type is "Ending"
                        val type = sellItemSnapshot.child("type").getValue(String::class.java)
                        if (type == "Ending") {
                            // Retrieve data from the snapshot and create an EndingItems object
                            val title = sellItemSnapshot.child("ItemName").getValue(String::class.java)!!
                            val price = sellItemSnapshot.child("price").getValue(String::class.java)!!
                            val imageUrl = sellItemSnapshot.child("imageURIs").children.firstOrNull()?.getValue(String::class.java)
                            val endingItem = EndingItems(title, price, imageUrl.toString())
                            endingArrayList.add(endingItem)
                        }
                    }
                }

                // Set up the adapter after retrieving data for all users
                lifecycleScope.launchWhenResumed {
                    adapter = EndingAdapter(this@EndingSoonFragment.requireContext(), endingArrayList)
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