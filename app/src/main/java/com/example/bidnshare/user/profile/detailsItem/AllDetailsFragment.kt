package com.example.bidnshare.user.profile.detailsItem

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.bidnshare.R
import com.example.bidnshare.adapter.BidItemAdapter
import com.example.bidnshare.databinding.FragmentAllDetailsBinding
import com.example.bidnshare.models.BidModels
import com.example.bidnshare.user.details.ImageAdapter2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AllDetailsFragment : Fragment() {
    private lateinit var binding : FragmentAllDetailsBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var imageAdapter: ImageAdapter2
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAllDetailsBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val timeStamp = arguments?.getString("timeStamp")
        val uid = arguments?.getString("uid")
        val image = arguments?.getString("image")
        retrieveDetails(timeStamp,uid,image)
        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(uid!!).child("mySellItems").child(timeStamp!!)
        Glide.with(this@AllDetailsFragment.requireContext())
            .load(image)
            .into(binding.imageDetails)
        imageAdapter = ImageAdapter2(emptyList()) // Pass an empty list initially
        binding.recycler.adapter = imageAdapter // Set adapter to RecyclerView

    }
    fun retrieveDetails(timeStamp: String?, uid: String?, image: String?) {
        if (timeStamp != null && uid != null) {
            val query = database.child("Users").child(uid).child("mySellItems")
                .orderByChild("timestamp")
                .equalTo(timeStamp)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (itemSnapshot in snapshot.children) {
                        val itemDetails = itemSnapshot.child("ItemDetails").value.toString()
                        val itemName = itemSnapshot.child("ItemName").value.toString()
                        val price = itemSnapshot.child("price").value.toString()
                        val imageURIs = itemSnapshot.child("imageURIs").children.map { it
                            .getValue(String::class.java) }
                        imageAdapter.setImageUrls(imageURIs)


                        val sellerQuery = database.child("Users").child(uid)
                        sellerQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(sellerSnapshot: DataSnapshot) {
                                val fullName = sellerSnapshot.child("fullName").value.toString()
                                val profile = sellerSnapshot.child("image").value.toString()

                                // Display seller's profile and name in UI
                                Glide.with(this@AllDetailsFragment.requireContext())
                                    .load(profile)
                                    .into(binding.circleImageView2)
                                binding.namePosted.text = fullName
                                binding.tvPrice.text = price
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle error
                            }
                        })
                        val bidsQuery = database.child("Users").child(uid).child("mySellItems").child(itemSnapshot.key.toString()).child("Bids")

                        bidsQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(bidsSnapshot: DataSnapshot) {
                                var highestBidPrice = 0.0
                                var bidStatus = false
                                for (bidSnapshot in bidsSnapshot.children) {
                                    val bidPriceString = bidSnapshot.child("bidPrice").getValue(String::class.java)
                                    val timestamp = bidSnapshot.child("timestamp").getValue(String::class.java)
                                    val bidStatusValue = bidSnapshot.child("bidStatus").getValue(Boolean::class.java)
                                    Toast.makeText(this@AllDetailsFragment.requireContext(), bidStatusValue
                                        .toString(), Toast
                                        .LENGTH_SHORT).show()


                                    // Check if bidPriceString is not null and can be converted to double
                                    val bidPrice = bidPriceString?.toDoubleOrNull() ?: 0.0
                                    if (bidPrice > highestBidPrice) {
                                        highestBidPrice = bidPrice
                                    }
                                }

                                // Update UI with the highest bid price
                                binding.tvPriceCurentBid.text = highestBidPrice.toString()

                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle error
                            }
                        })


                        // Example: Display the retrieved data in your UI
                        binding.tvTitle3.text = itemDetails
                        binding.tvTitle.text = itemName
                        binding.tvPrice.text = "₱ $price"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

}