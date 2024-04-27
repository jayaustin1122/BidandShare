package com.example.bidnshare.admin.accounts

import android.Manifest.permission.SEND_SMS
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bidnshare.databinding.FragmentAccountRequestsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import pub.devrel.easypermissions.EasyPermissions


class AccountRequestsFragment : Fragment(){
    private lateinit var binding : FragmentAccountRequestsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    // array list to hold events
    private lateinit var accArrayList : ArrayList<AccountsModel>

    //adapter
    private lateinit var adapter : AccountsRequestsAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountRequestsBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this@AccountRequestsFragment.requireContext())
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
                for (data in snapshot.children) {
                    // data as model
                    val model = data.getValue(AccountsModel::class.java)

                    // Check if the userType is "member"
                    if (model?.access == false) {
                        // add to array
                        accArrayList.add(model!!)
                    }
                }
                if (isAdded && context != null) {
                    // set up adapter
                    adapter = AccountsRequestsAdapter(
                        this@AccountRequestsFragment,
                        this@AccountRequestsFragment.requireContext(),
                        accArrayList,
                        findNavController(),

                    )

                    // set to recycler
                    binding.adminEventRv.setHasFixedSize(true)
                    binding.adminEventRv.layoutManager = LinearLayoutManager(context)
                    binding.adminEventRv.adapter = adapter

                    // Check if the array list is empty
                    if (accArrayList.isEmpty()) {
                        // Show the TextView if there are no requests
                        binding.noRequestsTextView.visibility = View.VISIBLE
                    } else {
                        // Hide the TextView if there are requests
                        binding.noRequestsTextView.visibility = View.GONE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

}