package com.example.bidnshare.user.details

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.bidnshare.R
import com.example.bidnshare.adapter.BidItemAdapter
import com.example.bidnshare.databinding.FragmentDetailsBinding
import com.example.bidnshare.models.BidModels
import com.example.bidnshare.service.BidStatusService
import com.example.bidnshare.user.viewmodels.BidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DetailsFragment : Fragment(),BidDialogFragment.OnBidAddedListener {
    private lateinit var binding: FragmentDetailsBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var imageAdapter: ImageAdapter2
    private lateinit var bidAdapter: BidItemAdapter
    private var onBidAddedListener: BidDialogFragment.OnBidAddedListener? = null
    private lateinit var bidViewModel: BidViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailsBinding.inflate(layoutInflater)
        return binding.root
    }
    private fun showTimerTextview(dbRef: DatabaseReference) {
        dbRef.child("countdownTimer").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val minutes = dataSnapshot.child("minutes").getValue(Long::class.java) ?: 0
                val seconds = dataSnapshot.child("seconds").getValue(Long::class.java) ?: 0

                // Display countdown timer in your TextView
                binding.tvTime.text = String.format("%02d:%02d", minutes, seconds)

                // Check if timer has reached "00:00"
                if (minutes == 0L && seconds == 0L) {
                    // Navigate to HomeFragment
                    findNavController().navigate(R.id.navUserFragment)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bidViewModel = ViewModelProvider(this).get(BidViewModel::class.java)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val timeStamp = arguments?.getString("timeStamp")
        val uid = arguments?.getString("uid")
        val image = arguments?.getString("image")
        attempt(uid,timeStamp)
        retrieveDetails(timeStamp,uid,image)
        showDataRecycler(timeStamp,uid,image)
        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(uid!!).child("mySellItems").child(timeStamp!!)
        showTimerTextview(dbRef)
        Glide.with(this@DetailsFragment.requireContext())
            .load(image)
            .into(binding.imageDetails)
        imageAdapter = ImageAdapter2(emptyList()) // Pass an empty list initially
        binding.recycler.adapter = imageAdapter // Set adapter to RecyclerView
        bidViewModel.uploadStatus.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                findNavController().navigate(R.id.navUserFragment)
            } else {
                // Handle failure
                Toast.makeText(requireContext(), "Error uploading bid price", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        binding.btnBid.setOnClickListener {
            showBidDialog(timeStamp.toString(),uid)
            findNavController().navigate(R.id.navUserFragment)
        }


    }
    private fun showDataRecycler(timeStamp: String?, uid: String?, image: String?) {
        val userBid = database.child("Users")
            .child(uid!!)
            .child("mySellItems")
            .child(timeStamp.toString())
            .child("Bids")

        userBid.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(userBids: DataSnapshot) {
                val bidItems = ArrayList<BidModels>()
                for (bidSnapshot in userBids.children) {
                    val bidUser = bidSnapshot.child("userBid").getValue(String::class.java)
                    val bidPrice = bidSnapshot.child("bidPrice").getValue(String::class.java)
                    val minsAgo = bidSnapshot.child("timestamp").getValue(String::class.java)

                    val refUser = FirebaseDatabase.getInstance().getReference("Users")
                        .child(bidUser!!)

                    refUser.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userData: DataSnapshot) {
                            val userName = userData.child("fullName").getValue(String::class.java)
                            val userImageUrl = userData.child("image").getValue(String::class.java)
                            bidItems.add(BidModels(userName!!, bidPrice.toString(), userImageUrl!!,minsAgo!!))

                            val adapter = BidItemAdapter(requireContext(), bidItems)
                            binding.recyclerBidders.adapter = adapter
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle error
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
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
                                Glide.with(this@DetailsFragment.requireContext())
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
                                    Toast.makeText(this@DetailsFragment.requireContext(), bidStatusValue
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

                                binding.tvTime.text
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle error
                            }
                        })

                        // Retrieve other data as needed
                        if (uid == auth.uid.toString()) {
                            binding.btnBid.visibility = View.GONE
                        } else {
                            binding.btnBid.visibility = View.VISIBLE
                        }

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
    private fun showBidDialog(valueToPass: String, uid: String?) {
        val dialogFragment = BidDialogFragment()
        val args = Bundle().apply {
            putString("timeStamp", valueToPass)
            putString("uid", uid)
            putString("price", binding.tvPriceCurentBid.text.toString())
            putString("initialPrice", binding.tvPrice.text.toString())
        }
        dialogFragment.arguments = args
        dialogFragment.show(childFragmentManager, "BidDialogFragment")
    }
    fun attempt(uid: String?, timeStamp: String?) {
        val userBid = database.child("Users").child(uid!!).child("mySellItems").child(timeStamp.toString()).child("Bids")

        userBid.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(userBids: DataSnapshot) {
                var userHasBid = false
                for (bidSnapshot in userBids.children) {
                    val bidUser = bidSnapshot.child("userBid").getValue(String::class.java)
                    Log.d("Debug", "Bid User: $bidUser, Current User: ${auth.uid}")
                    if (bidUser == auth.uid.toString()) {
                        userHasBid = true
                        break // Exit loop if user has placed a bid
                    }
                }

                if (userHasBid) {
                    binding.btnBid.visibility = View.GONE
                    binding.btnBid.text = "You Have Already Place a Bid"
                    binding.btnBid.isClickable = false
                } else {
                    binding.btnBid.visibility = View.VISIBLE
                    binding.btnBid.isClickable = true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
    override fun onBidAdded(userHasBid: Boolean) {
        if (userHasBid) {
            binding.btnBid.visibility = View.GONE
            binding.btnBid.text = "You Have Already Placed a Bid"
            binding.btnBid.isClickable = false
        } else {
            binding.btnBid.visibility = View.VISIBLE
            binding.btnBid.isClickable = true
        }
    }


}
