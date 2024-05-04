package com.example.bidnshare.user.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bidnshare.R
import com.example.bidnshare.adapter.PendingAdapter
import com.example.bidnshare.adapter.PurchasedAdapter
import com.example.bidnshare.databinding.FragmentPurchasedBinding
import com.example.bidnshare.models.PendingItem
import com.example.bidnshare.models.SoldItem
import com.example.bidnshare.user.viewmodels.ViewModelPending
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class PurchasedFragment : Fragment() {
    private lateinit var binding : FragmentPurchasedBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var viewModel: ViewModelPending
    private lateinit var adapter: PurchasedAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPurchasedBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        adapter = PurchasedAdapter(requireContext(), findNavController(), mutableListOf()) { isEmpty ->
            checkEmptyState(isEmpty)
        }

        viewModel = ViewModelProvider(this).get(ViewModelPending::class.java)

        binding.recycler.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PurchasedFragment.adapter
        }

        viewModel.pendingItems.observe(viewLifecycleOwner) { pendingItems ->
            adapter.setItems(pendingItems)
            adapter.notifyDataSetChanged()
        }

        getSoldItemsForCurrentUser()


        checkEmptyState()
    }
    private fun checkEmptyState(isEmpty: Any = adapter.itemCount == 0) {
        if (isEmpty as Boolean) {
            binding.tvNoPendings.visibility = View.VISIBLE
        } else {
            binding.tvNoPendings.visibility = View.GONE
        }
    }
    private fun getSoldItemsForCurrentUser() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserUid!!).child("SoldItems")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(itemsSnapshot: DataSnapshot) {
                val soldItems = mutableListOf<SoldItem>()
                if (itemsSnapshot.exists()) {
                    for (itemSnapshot in itemsSnapshot.children) {
                        val itemDetails = itemSnapshot.child("ItemDetails").getValue(String::class.java)
                        val itemName = itemSnapshot.child("ItemName").getValue(String::class.java)
                        val price = itemSnapshot.child("price").getValue(String::class.java)
                        // Retrieve other fields as needed

                        val soldItem: SoldItem? = itemDetails?.let { details ->
                            itemName?.let { name ->
                                price?.let { itemPrice ->
                                    SoldItem(details, name, itemPrice)
                                }
                            }
                        }

                        soldItem?.let {
                            soldItems.add(it)
                        }
                    }
                    // Handle sold items data (e.g., update UI, pass to ViewModel, etc.)
                    viewModel.setSoldItems(soldItems)
                } else {
                    // Handle the case where "SoldItems" path does not exist
                    // Example: showToast("No sold items found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event if needed
                Toast.makeText(requireContext(), "Failed to retrieve sold items: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}