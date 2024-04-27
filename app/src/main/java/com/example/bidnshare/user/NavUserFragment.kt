package com.example.bidnshare.user

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
import com.example.bidnshare.databinding.FragmentNavUserBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NavUserFragment : Fragment() {
    private lateinit var binding : FragmentNavUserBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var fragmentManager: FragmentManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNavUserBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        fragmentManager = requireActivity().supportFragmentManager
        var homeFragment = HomeUserFragment()
        var notifUserFragment = NotifUserFragment()
        var profileUserFragment = ProfileUserFragment()
        var chatsFragment = ChatsFragment()
        loadUsersInfo()
        val bottomNavigationView: BottomNavigationView = binding.bottomNavigation
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.navigation_Home -> homeFragment
                R.id.navigation_notif -> notifUserFragment
                R.id.navigation_profile -> profileUserFragment
                R.id.navigation_chat -> chatsFragment
                else -> return@setOnNavigationItemSelectedListener false
            }
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commitAllowingStateLoss() // Use commitAllowingStateLoss() to retain fragment state
            true
        }

        if (savedInstanceState == null) {
            // Initially load the HomeFragment only if it's not already added
            if (!homeFragment.isAdded) {
                fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, homeFragment)
                    .commit()
            }
            bottomNavigationView.selectedItemId = R.id.navigation_Home
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

                    //set data
                    binding.name.text = name
                    Glide.with(requireContext())
                        .load(image)
                        .into(binding.circleImageView)
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