package com.example.bidnshare.admin.accounts

import android.Manifest.permission.SEND_SMS
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bidnshare.R
import com.example.bidnshare.databinding.DialogAccountDetailBinding
import com.example.bidnshare.databinding.ItemAccountBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccountsRequestsAdapter(
    private val fragment: AccountRequestsFragment,
    private val context: Context,
    private var accountArrayList: List<AccountsModel>,
    private val navController: NavController
) : RecyclerView.Adapter<AccountsRequestsAdapter.ViewHolderAccounts>() {

    private lateinit var binding: ItemAccountBinding
    private val database = Firebase.database

    inner class ViewHolderAccounts(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = binding.tvID
        val image: ImageView = binding.imgPicture
    }
    private val requestCode = 123


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderAccounts {
        binding = ItemAccountBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolderAccounts(binding.root)
    }

    override fun getItemCount(): Int {
        return accountArrayList.size
    }

    override fun onBindViewHolder(holder: ViewHolderAccounts, position: Int) {
        val model = accountArrayList[position]
        val fullname = model.fullName
        val image = model.image
        val uid = model.uid
        val accessStatus = model.accessStatus
        val contact = model.phone

        if (accessStatus == "Restricted") {
            binding.restrict.visibility = View.VISIBLE
        }

        holder.apply {
            Glide.with(this@AccountsRequestsAdapter.context)
                .load(image)
                .into(binding.imgPicture)

            binding.tvID.text = fullname
            binding.btnMore.setOnClickListener {
                moreOptions(model, holder)

                itemView.setOnClickListener {
                    viewDetails(model, holder)
                }
            }
        }
        holder.itemView.setOnClickListener {
            viewDetails(model, holder)
        }
    }

    private fun viewDetails(model: AccountsModel, holder: ViewHolderAccounts) {
        val inflater = LayoutInflater.from(this@AccountsRequestsAdapter.context)
        val dialogBinding = DialogAccountDetailBinding.inflate(inflater)
        val dialog = Dialog(this@AccountsRequestsAdapter.context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)
        dialogBinding.tvName.text = model.fullName
        Glide.with(this@AccountsRequestsAdapter.context)
            .load(model.image)
            .into(dialogBinding.imgAccount)

        dialog.show()
    }

    private fun moreOptions(model: AccountsModel, holder: ViewHolderAccounts) {
        val access = model.access
        val uid = model.uid

        val options = arrayOf("Accept", "Delete")
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Option")
            .setItems(options) { dialog, position ->
                //handle item clicked
                if (position == 0) {
                    toggleRestriction(model)
                } else if (position == 1) {
                    //delete btn
                    //dialog
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete this Account?")
                        .setPositiveButton("Confirm") { a, d ->
                            Toast.makeText(context, "Account Deleted", Toast.LENGTH_SHORT).show()

                            deleteEvent(model, holder)
                        }
                        .setNegativeButton("Cancel") { a, d ->
                            a.dismiss()
                        }
                        .show()
                }
            }
            .show()

    }

    private fun toggleRestriction(model: AccountsModel) {
        val uid = model.uid

        // Retrieve current accessStatus from the database
        database.getReference("Users")
            .child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val currentAccessStatus =
                            dataSnapshot.child("access").getValue(Boolean::class.java)
                        val token = dataSnapshot.child("token").getValue(String::class.java)
                        val fullName = dataSnapshot.child("fullName").getValue(String::class.java)
                        val phoneNumber = dataSnapshot.child("phone").getValue(String::class.java)

                        val hashMap: HashMap<String, Any?> = HashMap()
                        hashMap["access"] = true

                        try {
                            database.getReference("Users")
                                .child(uid)
                                .updateChildren(hashMap)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        navController.apply {
                                            navigate(R.id.homeUserFragment)
                                        }
                                        Toast.makeText(
                                            this@AccountsRequestsAdapter.context,
                                            "Data Updated!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        if (token != null) {
                                            if (phoneNumber != null) {
                                                val permission = SEND_SMS
                                                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                                                    ActivityCompat.requestPermissions(fragment.requireActivity(), arrayOf(permission), requestCode)
                                                    if (fullName != null) {
                                                        sendSMS(fullName, "Your access in BidandShare has been granted.")
                                                    }
                                                } else {
                                                    // Permission already granted, proceed with sending SMS
                                                    if (fullName != null) {
                                                        sendSMS(fullName, "Your access in BidandShare has been granted.")
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Toast.makeText(
                                            this@AccountsRequestsAdapter.context,
                                            task.exception?.message ?: "Unknown error",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@AccountsRequestsAdapter.context,
                                "Error updating data: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors if needed
                }
            })
    }
    private fun sendSMS(phoneNumber: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(this@AccountsRequestsAdapter.context, "sms send", Toast.LENGTH_SHORT).show()
            Log.d("SendSMS", "Error sending SMS:$phoneNumber $message")
        } catch (e: Exception) {
            Log.e("SendSMS", "Error sending SMS: ${e.message}")
        }
    }
    private fun deleteEvent(model: AccountsModel, holder: ViewHolderAccounts) {
        val id = model.uid

        val dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "Unable to delete due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
