package com.example.bidnshare.ui

import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.bidnshare.R
import com.example.bidnshare.databinding.FragmentForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth


class ForgotPasswordFragment : Fragment() {
    private lateinit var binding : FragmentForgotPasswordBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var progressDialog : ProgressDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForgotPasswordBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this.requireContext())
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        binding.btnReset.setOnClickListener {
            val email = binding.edtForgotPasswordEmail.text.toString()

            if (email.isNotEmpty()) {
                resetPassword(email)
            } else {
                binding.edtForgotPasswordEmail.setText("")
                Toast.makeText(
                    this@ForgotPasswordFragment.requireContext(),
                    "Please Enter Email Address",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().apply {
                    popBackStack(
                        R.id.forgotPasswordFragment,
                        false
                    ) // Pop all fragments up to HomeFragment
                    navigate(R.id.signInFragment) // Navigate to LoginFragment
                }
            }
        }
        binding.btnForgotPasswordBack.setOnClickListener {
            findNavController().apply {
                popBackStack(
                    R.id.forgotPasswordFragment,
                    false
                ) // Pop all fragments up to HomeFragment
                navigate(R.id.signInFragment) // Navigate to LoginFragment
            }
        }
    }
    private fun resetPassword(email: String) {
        val progressDialog = ProgressDialog(this@ForgotPasswordFragment.requireContext()) // Assuming 'this' is your activity or context
        progressDialog.setMessage("Resetting...")
        progressDialog.show()

        val auth = FirebaseAuth.getInstance()

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                progressDialog.dismiss() // Dismiss the progress dialog once the task is complete

                if (task.isSuccessful) {
                    // Password reset email sent successfully
                    Toast.makeText(this@ForgotPasswordFragment.requireContext(), "Password reset email sent", Toast.LENGTH_SHORT).show()
                } else {
                    // If the task fails, display a message to the user
                    Toast.makeText(this@ForgotPasswordFragment.requireContext(), "Failed to send reset email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}