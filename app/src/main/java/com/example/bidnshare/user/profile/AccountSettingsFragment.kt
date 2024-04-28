package com.example.bidnshare.user.profile

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.bidnshare.R
import com.example.bidnshare.databinding.FragmentAccountSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AccountSettingsFragment : Fragment() {
    private lateinit var binding : FragmentAccountSettingsBinding
    private lateinit var auth: FirebaseAuth
    private var progressDialog: ProgressDialog? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountSettingsBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        loadUsersInfo()
        binding.btnUpdateProfile.setOnClickListener {
            progressDialog = ProgressDialog(requireContext())
            progressDialog?.setMessage("Updating user information...")
            progressDialog?.setCancelable(false)
            progressDialog?.show()
            updateUserInfo()
        }
    }
    private fun updateUserInfo() {
        val fullName = binding.etFullname.text.toString()
        val address = binding.etAddress.text.toString()
        val phone = binding.etPhone.text.toString()
        val password = binding.etPass.text.toString()

        //reference to the user's node
        val ref = FirebaseDatabase.getInstance().getReference("Users").child(auth.uid!!)

        // update user info
        val updates = hashMapOf<String, Any>(
            "fullName" to fullName,
            "address" to address,
            "phone" to phone,
            "password" to password
        )


        ref.updateChildren(updates)
            .addOnSuccessListener {
                progressDialog?.dismiss()
                Toast.makeText(
                    requireContext(),
                    "User information updated successfully",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().apply {
                    popBackStack(R.id.profileAdminFragment, false)
                    navigate(R.id.homeNavFragment)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Failed to update user information: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun loadUsersInfo() {
        //reference
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(auth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info

                    val image = "${snapshot.child("image").value}"
                    val name = "${snapshot.child("fullName").value}"
                    val phone = "${snapshot.child("phone").value}"
                    val address = "${snapshot.child("address").value}"
                    val pass = "${snapshot.child("password").value}"

                    //set data
                    binding.etFullname.setText(name)
                    binding.etAddress.setText(address)
                    binding.etPass.setText(pass)
                    binding.etPhone.setText(phone)
                    Glide.with(requireContext())
                        .load(image)
                        .into(binding.imgprofile)
                    Toast.makeText(
                        requireContext(),
                        "Welcome $name!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

}