package com.example.bidnshare.user.details

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.bidnshare.R
import com.example.bidnshare.databinding.DialogBidBinding
import com.example.bidnshare.user.viewmodels.BidViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class BidDialogFragment : BottomSheetDialogFragment() {
    private lateinit var binding: DialogBidBinding
    private val bidViewModel: BidViewModel by activityViewModels()
    private var onBidAddedListener: OnBidAddedListener? = null

    interface OnBidAddedListener {
        fun onBidAdded(userHasBid: Boolean)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogBidBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnBidAddedListener) {
            onBidAddedListener = context
        } else {
            throw RuntimeException("$context must implement OnBidAddedListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val timeStamps = arguments?.getString("timeStamp")
        val uid = arguments?.getString("uid")
        val price = arguments?.getString("price")
        val initialPrice = arguments?.getString("initialPrice")

        if (price != null && price.isNotBlank()) {
            binding.tvHighestBid.text = "Highest Bid is $price"
            binding.tvHighestBid.visibility = View.VISIBLE
        } else {
            binding.tvHighestBid.visibility = View.GONE
            binding.tvHighestBid.text = ""
        }
        binding.btnContinueSuccess.setOnClickListener {
            val bidPriceStr = binding.etPass.text.toString()
            val bidPrice = bidPriceStr.toDoubleOrNull()

            if (bidPrice != null) {
                if (price != null && bidPrice >= (price.toDoubleOrNull() ?: Double.MIN_VALUE)) {
                    bidViewModel.uploadBidPrice(bidPrice, timeStamps!!, uid.toString())
                    Toast.makeText(requireContext(), "Bid Added", Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "Bid price should be higher than the current price", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Enter Amount", Toast.LENGTH_SHORT).show()
            }
        }
    }
}