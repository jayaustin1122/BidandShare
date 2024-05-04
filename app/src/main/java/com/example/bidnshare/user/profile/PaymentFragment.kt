package com.example.bidnshare.user.profile
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.bidnshare.R
import com.example.bidnshare.databinding.FragmentPaymentBinding
import com.example.bidnshare.models.SoldItem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class PaymentFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentPaymentBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val PERMISSION_REQUEST_CODE = 123
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPaymentBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val timeStamp = arguments?.getString("timeStamp")
        val uid = arguments?.getString("uid")
        val image = arguments?.getString("image")
       val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(uid!!).child("mySellItems").child(timeStamp!!)

        // Request location permission if not granted
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        } else {
            getCurrentLocation()
        }

        // Continue button click listener
        binding.continueButton.setOnClickListener {
            if (!validatePaymentMethod() || !validateAddress()) {
                return@setOnClickListener
            }

            // If bank payment is selected, validate card details
            if (binding.bankPaymentRadioButton.isChecked) {
                if (!validateCardDetails()) {
                    return@setOnClickListener
                }
            }

            updateSellOrFree(dbRef,uid)
            retrieveAndUploadSoldItems(dbRef,uid)

        }

        // RadioGroup listener to toggle visibility of card details input
        binding.paymentMethodRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.bankPaymentRadioButton) {
                binding.cardDetailsInputLayout.visibility = View.VISIBLE
            } else {
                binding.cardDetailsInputLayout.visibility = View.GONE
            }
        }
    }
    private fun retrieveAndUploadSoldItems(dbRef: DatabaseReference, supplierUid: String) {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (itemSnapshot in snapshot.children) {
                        val itemDetails = itemSnapshot.child("ItemDetails").getValue(String::class.java)
                        val itemName = itemSnapshot.child("ItemName").getValue(String::class.java)
                        val price = itemSnapshot.child("price").getValue(String::class.java)
                        // Retrieve other data fields as needed

                        // Create a map to store the item data
                        val soldItemData = hashMapOf<String, Any?>(
                            "ItemDetails" to itemDetails,
                            "ItemName" to itemName,
                            "price" to price
                            // Add other fields as needed
                        )

                        // Upload data to supplier's node with additional path "SoldItems"
                        val supplierSoldItemsRef = FirebaseDatabase.getInstance().getReference("Users")
                            .child(supplierUid)
                            .child("SoldItems")
                            .push()
                        supplierSoldItemsRef.setValue(soldItemData)
                            .addOnSuccessListener {
                                // Successfully uploaded data
                            }
                            .addOnFailureListener { e ->
                                // Failed to upload data
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to upload sold item: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    Toast.makeText(requireContext(), "No data found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to retrieve data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun updateSellOrFree(dbRef: DatabaseReference, uid: String?) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        val addressClient = binding.addressEditText.text.toString()

        val soldAndBuyData = hashMapOf(
            "purchasedBy" to currentUserUid,
            "supplier" to uid,
            "addressClient" to addressClient
        )

        dbRef.child("sellOrFree").setValue("Purchased Items")
            .addOnSuccessListener {
                // If the first update is successful, update the second value
                dbRef.child("type").setValue("Sold")
                    .addOnSuccessListener {
                        // After updating orderedBy, also update SoldandBuy path
                        val soldAndBuyRef = dbRef.child("SoldandBuy")
                        soldAndBuyRef.push().setValue(soldAndBuyData)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Item Transfer to the Client",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Payment successful, navigate back or perform any other action
                                dismiss() // Close the PaymentFragment after successful payment
                            }.addOnFailureListener { e ->
                                // Handle failure of updating SoldandBuy
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to update SoldandBuy: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        // Handle failure of the orderedBy update
                        Toast.makeText(
                            requireContext(),
                            "Failed to update item type: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                // Handle failure of the sellOrFree update
                Toast.makeText(
                    requireContext(),
                    "Failed to update item sellOrFree: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }



    private fun validateCardDetails(): Boolean {
        val cardNumber = binding.cardDetailsEditText.text.toString().trim()
        if (cardNumber.isEmpty()) {
            binding.cardDetailsEditText.error = "Please enter card number"
            return false
        }
        binding.cardDetailsEditText.error = null
        return true
    }


    private fun getCurrentLocation() {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(
                this@PaymentFragment.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this@PaymentFragment.requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (location != null) {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses = geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            )
            val address = addresses?.get(0)?.getAddressLine(0)
            binding.addressEditText.setText(address)
        } else {
            Toast.makeText(
                requireContext(),
                "Unable to retrieve current location",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun validatePaymentMethod(): Boolean {
        if (!binding.cashOnDeliveryRadioButton.isChecked && !binding.bankPaymentRadioButton.isChecked) {
            Toast.makeText(
                requireContext(),
                "Please select a payment method",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    private fun validateAddress(): Boolean {
        val address = binding.addressEditText.text.toString().trim()
        if (address.isEmpty()) {
            binding.addressInputLayout.error = "Please enter your address"
            return false
        }
        binding.addressInputLayout.error = null
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        }
    }
}
