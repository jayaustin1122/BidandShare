package com.example.bidnshare.admin

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.example.bidnshare.R
import com.example.bidnshare.admin.accounts.AccountsFragment
import com.example.bidnshare.databinding.FragmentHomeNavBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeNavFragment : Fragment() {
    private lateinit var binding : FragmentHomeNavBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var fragmentManager: FragmentManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeNavBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        fragmentManager = requireActivity().supportFragmentManager
        val homeAdminFragment = HomeAdminFragment()
        val accountsFragment = AccountsFragment()
        val profileAdminFragment = ProfileAdminFragment()
        loadUsersInfo()
        val bottomNavigationView: BottomNavigationView = binding.bottomNavigation
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.navigation_HomeAdmin -> homeAdminFragment
                R.id.navigation_accAdmin -> accountsFragment
                R.id.navigation_profileAdmin -> profileAdminFragment
                else -> return@setOnNavigationItemSelectedListener false
            }
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_containerAdmin, selectedFragment)
                .commitAllowingStateLoss() // Use commitAllowingStateLoss() to retain fragment state
            true
        }

        if (savedInstanceState == null) {
            // Initially load the HomeFragment only if it's not already added
            if (!homeAdminFragment.isAdded) {
                fragmentManager.beginTransaction()
                    .add(R.id.fragment_containerAdmin, homeAdminFragment)
                    .commit()
            }
            bottomNavigationView.selectedItemId = R.id.navigation_HomeAdmin
        }
    }
    private fun loadUsersInfo() {
        if (!isAdded) return // Check if the fragment is added

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(auth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return // Check again before performing UI operations

                    val image = "${snapshot.child("image").value}"
                    val name = "${snapshot.child("fullName").value}"

                    binding.name.text = name
                    Glide.with(this@HomeNavFragment.requireContext())
                        .load(image)
                        .into(binding.circleImageView)
                    Toast.makeText(
                        requireContext(),
                        "Welcome $name!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled
                }
            })
    }

}