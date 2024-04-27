package com.example.bidnshare.ui.signup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.example.bidnshare.R
import com.example.bidnshare.databinding.FragmentFourSignupBinding

class FourSignupFragment : Fragment() {
    private lateinit var binding: FragmentFourSignupBinding
    private var isMaleSelected: Boolean = false
    private var isFemaleSelected: Boolean = false
    private lateinit var viewModel: SignUpViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFourSignupBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(SignUpViewModel::class.java)
    }    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imgMale.setOnClickListener {
            if (!isMaleSelected) {
                selectMale()
                deselectFemale()
                viewModel.gender = "Male" // Set gender in the view model to "Male"
            }
        }

        binding.imgFemale.setOnClickListener {
            if (!isFemaleSelected) {
                selectFemale()
                deselectMale()
                viewModel.gender = "Female" // Set gender in the view model to "Female"
            }
        }
    }

    private fun selectMale() {
        binding.imgMale.alpha = 1.0f // Set alpha to 1 to indicate selection
        isMaleSelected = true
    }

    private fun deselectMale() {
        binding.imgMale.alpha = 0.2f // Set alpha to 0.5 to indicate deselection
        isMaleSelected = false
    }

    private fun selectFemale() {
        binding.imgFemale.alpha = 1.0f // Set alpha to 1 to indicate selection
        isFemaleSelected = true
    }

    private fun deselectFemale() {
        binding.imgFemale.alpha = 0.2f // Set alpha to 0.5 to indicate deselection
        isFemaleSelected = false
    }
}
