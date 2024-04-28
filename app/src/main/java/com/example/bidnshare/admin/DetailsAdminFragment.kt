package com.example.bidnshare.admin

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.bidnshare.R
import com.example.bidnshare.adapter.BidItemAdapter
import com.example.bidnshare.databinding.FragmentDetailsAdminBinding
import com.example.bidnshare.models.BidModels
import com.example.bidnshare.service.CountdownService
import com.example.bidnshare.user.details.ImageAdapter2
import com.example.bidnshare.user.viewmodels.BidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class DetailsAdminFragment : Fragment() {
    private lateinit var binding : FragmentDetailsAdminBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var imageAdapter: ImageAdapter2
    private lateinit var bidViewModel: BidViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailsAdminBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bidViewModel = ViewModelProvider(this).get(BidViewModel::class.java)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val timeStamp = arguments?.getString("timeStamp")
        val uid = arguments?.getString("uid")
        val image = arguments?.getString("image")
        val sellOrFree = arguments?.getString("sellOrFree")
        retrieveDetails(timeStamp,uid,image)
        showDataRecycler(timeStamp,uid,image)
        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(uid!!).child("mySellItems").child(timeStamp!!)
        if (sellOrFree == "Live") {
            binding.imgBtnLive.setColorFilter(ContextCompat.getColor(requireContext(), R.color.g_red))
            binding.imgBtnLive.isEnabled = false
        } else {
            // Reset the color of the ImageButton
            binding.imgBtnLive.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black))
        }
        Glide.with(this@DetailsAdminFragment.requireContext())
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
        binding.imgBtnLive.setOnClickListener {
            showAsk(dbRef,timeStamp,uid)

        }

    }
    fun showAsk(dbRef: DatabaseReference, timeStamp: String, uid: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Live?")
            .setMessage("Are you sure you want to Live Auction this Item?")
            .setPositiveButton("Confirm") { a, d ->

                updateInDb(dbRef,timeStamp,uid)
                Toast.makeText(requireContext(), "Item Updated", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { a, d ->
                a.dismiss()
            }
            .show()
    }

    private fun updateInDb(dbRef: DatabaseReference, timeStamp: String, uid: String) {
        // Update the type field in the database
        dbRef.child("sellOrFree").setValue("Live")
            .addOnSuccessListener {
                // Handle successful update
                Toast.makeText(context, "Item Updated in Database", Toast.LENGTH_SHORT).show()
                binding.imgBtnLive.setColorFilter(ContextCompat.getColor(requireContext(), R.color.g_red))
                showTimeLimitDialog(dbRef,timeStamp,uid)
            }
            .addOnFailureListener { e ->
                // Handle failed update
                Toast.makeText(context, "Failed to update item: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showTimeLimitDialog(dbRef: DatabaseReference, timeStamp: String, uid: String) {
        val options = arrayOf("1 minute", "10 minutes", "15 minutes")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose Auction Duration")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> gotoService(dbRef,  1,timeStamp,uid) // 5 minutes
                    1 -> gotoService(dbRef, 10, timeStamp, uid) // 10 minutes
                    2 -> gotoService(dbRef, 15, timeStamp, uid) // 15 minutes

                }
            }
            .show()
    }




    private fun gotoService(dbRef: DatabaseReference, i: Int, timeStamp: String, uid: String) {
        val intent = Intent(requireContext(), CountdownService::class.java)
        intent.putExtra("timeStamp", timeStamp) // Corrected
        intent.putExtra("uid", uid) // Corrected
        intent.putExtra("durationMinutes", i)
        ContextCompat.startForegroundService(requireContext(), intent)
        Toast.makeText(context, "Iasdasd", Toast.LENGTH_SHORT).show()
    }

    private fun startCountdownTimer(auctionEndTimeMillis: Long, dbRef: DatabaseReference) {
        val countdownTimer = object : CountDownTimer(auctionEndTimeMillis - System.currentTimeMillis(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Convert milliseconds to minutes and seconds
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                // Display countdown timer in your TextView
                binding.tvTime.text = String.format("%02d:%02d", minutes, seconds)

                // Update countdown timer in the database
                val timerMap = mapOf("minutes" to minutes, "seconds" to seconds)
                dbRef.child("countdownTimer").setValue(timerMap)
            }

            override fun onFinish() {
                // Handle auction finish
                binding.tvTime.text = "00:00"
                showTimerTextview(dbRef)
                // Optionally, perform any actions after the auction ends
                retrieveHighestBidder(dbRef)
            }
        }
        countdownTimer.start()
    }

    private fun showTimerTextview(dbRef: DatabaseReference) {
        dbRef.child("countdownTimer").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val minutes = dataSnapshot.child("minutes").getValue(Long::class.java) ?: 0
                val seconds = dataSnapshot.child("seconds").getValue(Long::class.java) ?: 0

                // Display countdown timer in your TextView
                binding.tvTime.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }


    private fun retrieveHighestBidder(dbRef: DatabaseReference) {
        dbRef.child("Bids")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(bidsSnapshot: DataSnapshot) {
                    var highestBidPriceString: String? = null
                    var highestBidderUid: String? = null

                    for (bidSnapshot in bidsSnapshot.children) {
                        val bidPriceString = bidSnapshot.child("bidPrice").getValue(String::class.java)
                        val bidderId = bidSnapshot.child("userBid").getValue(String::class.java)

                        if (bidPriceString != null) {
                            val bidPrice = bidPriceString.toDoubleOrNull() ?: 0.0
                            if (bidPrice > (highestBidPriceString?.toDoubleOrNull() ?: 0.0)) {
                                highestBidPriceString = bidPriceString
                                highestBidderUid = bidderId
                            }
                        }
                    }

                    if (highestBidderUid != null) {
                        // If the highest bidder's ID is found, pass it to retrieveBidderInfo function.
                        retrieveBidderInfo(highestBidderUid,dbRef)
                    } else {
                        // Handle case where there are no bids.
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }



    private fun retrieveBidderInfo(uid: String?, dbRef: DatabaseReference) {
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid!!)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bidderName = snapshot.child("fullName").getValue(String::class.java)
                val uid = snapshot.child("uid").getValue(String::class.java)
                // Show dialog with bidder's name
                showDialogWithBidderName(bidderName)
                updateSellOrFree(dbRef,uid)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun updateSellOrFree(dbRef: DatabaseReference, uid: String?) {
        dbRef.child("sellOrFree").setValue("Ordered")
            .addOnSuccessListener {
                // If the first update is successful, update the second value
                dbRef.child("type").setValue("Ending")
                    .addOnSuccessListener {
                        // Handle successful update of both values
                        dbRef.child("orderedBy").setValue(uid)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    "Item Transfer to the Client",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                            .addOnFailureListener { e ->
                                // Handle failure of the second update
                                Toast.makeText(
                                    context,
                                    "Failed to update item type: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
            }
            .addOnFailureListener { e ->
                // Handle failure of the first update
                Toast.makeText(context, "Failed to update item sellOrFree: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDialogWithBidderName(bidderName: String?) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Auction Ended")
            .setMessage("The auction has ended. The highest bidder is: $bidderName")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                findNavController().apply {
                    popBackStack(
                        R.id.splashFragment,
                        false
                    ) // Pop all fragments up to HomeFragment
                    navigate(R.id.homeNavFragment) // Navigate to LoginFragment
                }
                // Optionally, perform any action after user clicks OK
            }
            .setCancelable(false) // Prevent dismissing the dialog by tapping outside
            .show()

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
            val queryItem = database.child("Users").child(uid).child("mySellItems")
                .orderByChild("timestamp")
                .equalTo(timeStamp)

            queryItem.addListenerForSingleValueEvent(object : ValueEventListener {
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
                                Glide.with(this@DetailsAdminFragment.requireContext())
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
                                    Toast.makeText(this@DetailsAdminFragment.requireContext(), bidStatusValue
                                        .toString(), Toast
                                        .LENGTH_SHORT).show()

                                    Toast.makeText(requireContext(),bidStatusValue.toString(),Toast.LENGTH_SHORT).show()
                                    if (bidStatusValue == true) {
                                        bidStatus = true

                                    }
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