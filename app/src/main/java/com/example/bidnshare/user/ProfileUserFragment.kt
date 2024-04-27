package com.example.bidnshare.user

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.bidnshare.R
import com.example.bidnshare.databinding.FragmentMyProfileBinding
import com.example.bidnshare.databinding.FragmentProfileUserBinding
import com.example.bidnshare.user.profile.AccountSettingsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ProfileUserFragment : Fragment() {
    private lateinit var binding : FragmentMyProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog : ProgressDialog
    private lateinit var fragmentManager: FragmentManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyProfileBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentManager = requireActivity().supportFragmentManager
        auth = FirebaseAuth.getInstance()
        val accountSettingsFragment = AccountSettingsFragment()
        progressDialog = ProgressDialog(this.requireContext())
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        loadUsersInfo()
        binding.btnLogout.setOnClickListener {
            progressDialog.setMessage("Logging Out...")
            progressDialog.show()
            view.postDelayed({
                auth.signOut()
                progressDialog.dismiss()

                findNavController().apply {
                    popBackStack(R.id.myProfileFragment, false)
                    navigate(R.id.signInFragment)
                }
            }, 2000)
        }
        binding.cvItemSold.setOnClickListener {
            findNavController().navigate(R.id.itemSoldFragment)
        }
        binding.cvPending.setOnClickListener {
            findNavController().navigate(R.id.pendingTransactionsFragment)
        }
        binding.cvPurchasedItems.setOnClickListener {
            findNavController().navigate(R.id.purchasedFragment)
        }
        binding.cvAccountSettings.setOnClickListener {
            fragmentManager.beginTransaction()
                .add(R.id.fragment_container, accountSettingsFragment)
                .commit()
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
                    val address = "${snapshot.child("address").value}"

                    //set data
                    binding.tvName2.text = name
                    binding.tvAddress2.text = address
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