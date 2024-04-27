package com.example.bidnshare.ui.signup

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.example.bidnshare.R
import com.example.bidnshare.databinding.FragmentOneSignupBinding
import com.example.bidnshare.ui.SignUpFragment


class OneSignupFragment : Fragment() {
    private lateinit var binding: FragmentOneSignupBinding
    private lateinit var viewModel: SignUpViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOneSignupBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(SignUpViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Assuming etFullname, etEmailSignUp, etPasswordSignUp are your EditText fields
        binding.etFullname.addTextChangedListener {
            viewModel.fullname = it.toString()
        }

        binding.etEmailSignUp.addTextChangedListener {
            viewModel.email = it.toString()
        }

        binding.etPasswordSignUp.addTextChangedListener {
            viewModel.password = it.toString()
        }
    }
}




