package com.example.bidnshare.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bidnshare.R
import com.example.bidnshare.admin.accounts.AccountsModel
import com.example.bidnshare.databinding.FragmentChatsBinding
import com.example.bidnshare.user.chat.MessengerUserAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ChatsFragment : Fragment() {
    private lateinit var binding : FragmentChatsBinding
    private lateinit var accArrayList : ArrayList<AccountsModel>
    private lateinit var auth : FirebaseAuth
    //adapter
    private lateinit var adapter : MessengerUserAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatsBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        loadUserInfo()
    }
    private fun loadUserInfo() {
        //initialize
        accArrayList = ArrayList()
        val authhh = auth.currentUser
        val dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear list
                accArrayList.clear()

                for (data in snapshot.children) {
                    // data as model
                    val model = data.getValue(AccountsModel::class.java)

                    if (model?.uid != authhh?.uid) {
                        // add to array
                        accArrayList.add(model!!)
                    }
                }
                //set up adapter
                adapter = MessengerUserAdapter(requireContext(),accArrayList,findNavController())
                //set to recycler
                binding.userRecyclerView.setHasFixedSize(true)
                binding.userRecyclerView.layoutManager = LinearLayoutManager(context)
                binding.userRecyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}