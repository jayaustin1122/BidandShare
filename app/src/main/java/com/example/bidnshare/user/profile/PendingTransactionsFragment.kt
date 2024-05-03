package com.example.bidnshare.user.profile
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bidnshare.adapter.PendingAdapter
import com.example.bidnshare.databinding.FragmentPendingTransactionsBinding
import com.example.bidnshare.models.PendingItem
import com.example.bidnshare.user.viewmodels.ViewModelPending
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PendingTransactionsFragment : Fragment() {
    private lateinit var binding: FragmentPendingTransactionsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var viewModel: ViewModelPending
    private lateinit var adapter: PendingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPendingTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        viewModel = ViewModelProvider(this).get(ViewModelPending::class.java)
        adapter = PendingAdapter(requireContext(), mutableListOf())

        binding.recycler.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PendingTransactionsFragment.adapter
        }

        viewModel.pendingItems.observe(viewLifecycleOwner) { pendingItems ->
            adapter.setItems(pendingItems)
            adapter.notifyDataSetChanged()
        }

        getAllLiveSellItems()
        adapter.setTotalPriceTextView(binding.amount)
        adapter.setPayButton(binding.btnPay)
    }

    private fun getAllLiveSellItems() {
        val dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(usersSnapshot: DataSnapshot) {
                val pendingItems = mutableListOf<PendingItem>()
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
                            val type = if (typeSnapshot.exists()) typeSnapshot.getValue(String::class.java)!! else ""
                            val pendingItem = PendingItem(title, time, imageUrl, price, timeStamp, uid, sellOrFree, type)
                            pendingItems.add(pendingItem)
                             Toast.makeText(requireContext(), "$title, $time, $imageUrl, $price, $timeStamp, $uid, $sellOrFree, $type", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                viewModel.setPendingItems(pendingItems)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event if needed
            }
        })
    }
}
