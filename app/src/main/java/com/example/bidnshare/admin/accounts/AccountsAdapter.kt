package com.example.bidnshare.admin.accounts
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bidnshare.databinding.DialogAccountDetailBinding
import com.example.bidnshare.databinding.ItemAccountBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AccountsAdapter (
    private val fragment: AccountsListsFragment,
    private val context: Context,
    private var accountArrayList: List<AccountsModel>,
    private val navController: NavController
):
    RecyclerView.Adapter<AccountsAdapter.ViewHolderAccounts>(){
    private lateinit var binding : ItemAccountBinding
    private val database = Firebase.database
    inner class ViewHolderAccounts(itemView: View): RecyclerView.ViewHolder(itemView) {
        val name : TextView = binding.tvID
        val image : ImageView = binding.imgPicture
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderAccounts {
        val inflater = LayoutInflater.from(parent.context)
        binding = ItemAccountBinding.inflate(inflater,parent,false)
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

        if (accessStatus == "Restricted"){
            binding.restrict.visibility = View.VISIBLE
        }

        holder.apply {
            Glide.with(this@AccountsAdapter.context)
                .load(image)
                .into(binding.imgPicture)

            binding.tvID.text = fullname
            binding.btnMore.setOnClickListener {
                moreOptions(model,holder)


            }
        }
        holder.itemView.setOnClickListener {
            viewDetails(model,holder)
        }
    }

    private fun viewDetails(model: AccountsModel, holder: AccountsAdapter.ViewHolderAccounts) {
        val inflater = LayoutInflater.from(this@AccountsAdapter.context)
        val dialogBinding = DialogAccountDetailBinding.inflate(inflater)
        val dialog = Dialog(this@AccountsAdapter.context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)
        dialogBinding.tvName.text = model.fullName
        Glide.with(this@AccountsAdapter.context)
            .load(model.image)
            .into(dialogBinding.imgAccount)
        dialogBinding.email.text = model.email
        dialogBinding.tvMobileNumber.text = model.phone
        dialogBinding.tvAddress.text = model.address
        dialogBinding.tvIdNumber.text = model.userID
        dialogBinding.tvUserType.text = model.userType

        dialog.show()
    }

    private fun moreOptions(model: AccountsModel, holder: AccountsAdapter.ViewHolderAccounts) {
        val access = model.access
        val uid = model.uid


        val options = arrayOf("Make this User Admin/Member","Restrict","Delete")
        val  builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Option")
            .setItems(options){dialog,position ->
                //handle item clicked
                if (position == 0 ){
                    makeUser(model)
                }
                else if (position == 1 ){
                    toggleRestriction(model)
                }
                else if (position == 2){
                    //delete btn
                    //dialog
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete this Account?")
                        .setPositiveButton("Confirm"){a,d->
                            Toast.makeText(context,"Account Deleted", Toast.LENGTH_SHORT).show()

                            deleteEvent(model,holder)
                        }
                        .setNegativeButton("Cancel"){a,d->
                            a.dismiss()
                        }
                        .show()
                }
            }
            .show()

    }

    private fun makeUser(model: AccountsModel) {
        val uid = model.uid

        // Retrieve current userType from the database
        database.getReference("Users")
            .child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val currentUserType = dataSnapshot.child("userType").getValue(String::class.java)

                        // Toggle between "Admin" and "Member"
                        val newUserType =
                            if (currentUserType == "admin") "member" else "admin"

                        val hashMap: HashMap<String, Any?> = HashMap()
                        hashMap["userType"] = newUserType

                        try {
                            database.getReference("Users")
                                .child(uid)
                                .updateChildren(hashMap)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            this@AccountsAdapter.context,
                                            "User type updated!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            this@AccountsAdapter.context,
                                            task.exception?.message ?: "Unknown error",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@AccountsAdapter.context,
                                "Error updating user type: ${e.message}",
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

    private fun toggleRestriction(model: AccountsModel) {
        val uid = model.uid

        // Retrieve current accessStatus from the database
        database.getReference("Users")
            .child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val currentAccessStatus = dataSnapshot.child("accessStatus").getValue(String::class.java)

                        // Toggle between "Restricted" and "Unrestricted"
                        val newAccessStatus = if (currentAccessStatus == "Restricted") "Unrestricted" else "Restricted"

                        val hashMap: HashMap<String, Any?> = HashMap()
                        hashMap["accessStatus"] = newAccessStatus

                        try {
                            database.getReference("Users")
                                .child(uid)
                                .updateChildren(hashMap)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            this@AccountsAdapter.context,
                                            "Data Updated!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            this@AccountsAdapter.context,
                                            task.exception?.message ?: "Unknown error",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@AccountsAdapter.context,
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
    private fun deleteEvent(model: AccountsModel, holder: AccountsAdapter.ViewHolderAccounts) {
        //id as the reference to delete

        val id = model.uid

        val dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context,"Deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                Toast.makeText(context,"Unable to delete due to ${e.message}", Toast.LENGTH_SHORT).show()

            }
    }
}