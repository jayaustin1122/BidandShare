package com.example.bidnshare.ui.signup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.example.bidnshare.R
import com.example.bidnshare.databinding.FragmentThreeSignupBinding


class ThreeSignupFragment : Fragment() {
    private lateinit var binding : FragmentThreeSignupBinding
    private lateinit var viewModel: SignUpViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentThreeSignupBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.etMonth.addTextChangedListener {
            viewModel.month = it.toString()
        }

        binding.etDay.addTextChangedListener {
            viewModel.day = it.toString()
        }

        binding.etYear.addTextChangedListener {
            viewModel.year = it.toString()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(SignUpViewModel::class.java)
    }
}