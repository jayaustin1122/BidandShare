package com.example.bidnshare.admin.accounts

import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bidnshare.databinding.FragmentAccountsListsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class AccountsListsFragment : Fragment() {
    private lateinit var binding : FragmentAccountsListsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    // array list to hold events
    private lateinit var accArrayList : ArrayList<AccountsModel>

    //adapter
    private lateinit var adapter : AccountsAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountsListsBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this@AccountsListsFragment.requireContext())
        progressDialog.setTitle("PLease wait")
        progressDialog.setCanceledOnTouchOutside(false)
        getAccounts()
    }
    private fun getAccounts() {
        //initialize
        accArrayList = ArrayList()

        val dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear list
                accArrayList.clear()
                for (data in snapshot.children){
                    //data as model
                    val model = data.getValue(AccountsModel::class.java)

                    // Check if the userType is "member"
                    if (model?.access == true) {
                        // add to array
                        accArrayList.add(model!!)
                    }
                }
                if (isAdded && context != null) {
                    //set up adapter
                    adapter = AccountsAdapter(this@AccountsListsFragment,this@AccountsListsFragment.requireContext(), accArrayList, findNavController())
                    //set to recycler
                    binding.adminEventRv.setHasFixedSize(true)
                    binding.adminEventRv.layoutManager = LinearLayoutManager(context)
                    binding.adminEventRv.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}