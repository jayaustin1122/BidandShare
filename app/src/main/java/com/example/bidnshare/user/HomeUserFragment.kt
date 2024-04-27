package com.example.bidnshare.user

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bidnshare.R
import com.example.bidnshare.adapter.FreeItemAdapter
import com.example.bidnshare.adapter.LiveAuctionAdapter
import com.example.bidnshare.databinding.FragmentHomeUserBinding
import com.example.bidnshare.models.FreeItems
import com.example.bidnshare.models.sellItem
import com.example.bidnshare.user.homeitemtabs.EndingSoonFragment
import com.example.bidnshare.user.homeitemtabs.NewlyListedFragment
import com.example.bidnshare.user.homeitemtabs.UpcomingFragment
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class HomeUserFragment : Fragment() {

    private lateinit var binding : FragmentHomeUserBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    // array list to hold events
    private lateinit var eventArrayList : ArrayList<sellItem>
    private lateinit var freeItems : ArrayList<FreeItems>
    private lateinit var tabLayout: TabLayout
    private var selectedTabIndex: Int = 0
    //adapter
    private lateinit var adapter : LiveAuctionAdapter
    private lateinit var adapter2 : FreeItemAdapter
    private lateinit var newsArrayList : ArrayList<sellItem>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeUserBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        tabLayout = binding.tbLayout
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this@HomeUserFragment.requireContext())
        progressDialog.setTitle("PLease wait")
        progressDialog.setCanceledOnTouchOutside(false)
        // Initialize the adapter here
        selectedTabIndex = savedInstanceState?.getInt("selectedTabIndex", 0) ?: 0
        adapter = LiveAuctionAdapter(this@HomeUserFragment.requireContext(),findNavController(), ArrayList())
        binding.recyclerliveauction.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerliveauction.layoutManager = layoutManager
        binding.recyclerliveauction.adapter = adapter
        getAllFreeItems()
        getAllLiveSellItems()
        showFragment(selectedTabIndex)
        addTabs()
        binding.tbLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    showFragment(it.position)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        binding.floatingActionButton.setOnClickListener {
            val addDataDialog = AddNewItemFragment()
            val bundle = Bundle()
            addDataDialog.arguments = bundle
            addDataDialog.show(childFragmentManager, "AddDataDialog")
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the selected tab index to restore it later
        outState.putInt("selectedTabIndex", selectedTabIndex)
    }

    private fun showFragment(position: Int) {
        val fragment = when (position) {
            0 -> NewlyListedFragment()
            1 -> UpcomingFragment()
            2 -> EndingSoonFragment()
            else -> NewlyListedFragment() // Default to LogsFragment
        }
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.viewPager, fragment)
            .commit()
        selectedTabIndex = position
    }
    private fun addTabs() {
        // Add tab items dynamically
        tabLayout.addTab(tabLayout.newTab().setText("Newly listed"))
        tabLayout.addTab(tabLayout.newTab().setText("Upcoming"))
        tabLayout.addTab(tabLayout.newTab().setText("Ending Soon"))
    }

    private fun getAllFreeItems() {
        // Initialize the freeItems ArrayList
        val freeItems = ArrayList<FreeItems>()

        // Get a reference to the Firebase database
        val dbRef = FirebaseDatabase.getInstance().getReference("Users")

        // Add a ValueEventListener to retrieve data for all users
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(usersSnapshot: DataSnapshot) {
                // Iterate through each user
                for (userSnapshot in usersSnapshot.children) {
                    // Get a reference to the sellItems for the current user
                    val userSellItemsRef = userSnapshot.child("mySellItems")

                    // Iterate through each sell item for the current user
                    for (sellItemSnapshot in userSellItemsRef.children) {
                        // Check if sellOrFree is "Free"
                        val sellOrFree = sellItemSnapshot.child("sellOrFree").getValue(String::class.java)
                        if (sellOrFree == "Free") {
                            // Retrieve data from the snapshot and create a FreeItem object
                            val title = sellItemSnapshot.child("ItemName").getValue(String::class.java)!!
                            val price = sellItemSnapshot.child("price").getValue(String::class.java)!!
                            val imageUrl = sellItemSnapshot.child("imageURIs").children.firstOrNull()?.getValue(String::class.java)
                            val freeItem = FreeItems(title, price, imageUrl.toString())
                            freeItems.add(freeItem)
                        }
                    }
                }

                // Set up the adapter after retrieving data for all users
                lifecycleScope.launchWhenResumed {
                    adapter2 = FreeItemAdapter(this@HomeUserFragment.requireContext(), freeItems)
                    binding.recycler.setHasFixedSize(true)
                    val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    binding.recycler.layoutManager = layoutManager
                    binding.recycler.adapter = adapter2
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event if needed
            }
        })
    }



    private fun getAllLiveSellItems() {
        // Initialize the eventArrayList
        val eventArrayList = ArrayList<sellItem>()

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
                        // Check if sellOrFree is "Live"
                        val sellOrLive = sellItemSnapshot.child("sellOrFree").getValue(String::class.java)
                        if (sellOrLive == "Live") {
                            // Retrieve data from the snapshot and create a SellItem object
                            val title = sellItemSnapshot.child("ItemName").getValue(String::class.java)!!
                            val time = sellItemSnapshot.child("currentTime").getValue(String::class.java)!!
                            val imageUrl = sellItemSnapshot.child("imageURIs").children.firstOrNull()?.getValue(String::class.java)
                            val price = sellItemSnapshot.child("price").getValue(String::class.java)!!
                            val timeStamp = sellItemSnapshot.child("timestamp").getValue(String::class.java)!!
                            val uid = sellItemSnapshot.child("currentUser").getValue(String::class.java)!!
                            val sellItem = sellItem(title, price, imageUrl.toString(), time, timeStamp, uid)

                            eventArrayList.add(sellItem)
                        }
                    }
                }

                // Set up the adapter after retrieving data for all users
                lifecycleScope.launchWhenResumed {
                    adapter = LiveAuctionAdapter(this@HomeUserFragment.requireContext(), findNavController(), eventArrayList)
                    binding.recyclerliveauction.setHasFixedSize(true)
                    val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    binding.recyclerliveauction.layoutManager = layoutManager
                    binding.recyclerliveauction.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event if needed
            }
        })
    }




}