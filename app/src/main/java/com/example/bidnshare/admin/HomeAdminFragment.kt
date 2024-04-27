package com.example.bidnshare.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bidnshare.adapter.LiveAuctionAdapterAdmin
import com.example.bidnshare.databinding.FragmentHomeAdminBinding
import com.example.bidnshare.user.viewmodels.SellItemViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class HomeAdminFragment : Fragment() {
    private lateinit var binding : FragmentHomeAdminBinding
    private lateinit var sellItemAdapter: LiveAuctionAdapterAdmin
    private lateinit var viewModel: SellItemViewModel

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeAdminBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        viewModel = ViewModelProvider(this).get(SellItemViewModel::class.java)
        binding.recyclerSells.setHasFixedSize(true)
        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerSells.layoutManager = layoutManager

        // Observe LiveData and update RecyclerView
        viewModel.getSellItemsLiveData().observe(viewLifecycleOwner, Observer { sellItems ->
            // Update RecyclerView adapter with the new data
            sellItemAdapter = LiveAuctionAdapterAdmin(requireContext(), findNavController(), sellItems)
            binding.recyclerSells.adapter = sellItemAdapter
        })

        // Fetch data from ViewModel
        viewModel.getAllLiveSellItems()
    }

}
