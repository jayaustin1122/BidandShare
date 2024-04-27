package com.example.bidnshare.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bidnshare.R
import com.example.bidnshare.databinding.DialogReviewBinding
import com.example.bidnshare.databinding.FragmentSignUpBinding
import com.example.bidnshare.databinding.FragmentSplashBinding
import com.example.bidnshare.user.NavUserFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SplashFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding : FragmentSplashBinding
    private lateinit var progressDialog : ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this.requireContext())
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        binding.buttonLoginAccountOptions.setOnClickListener {
                checkUser()
        }
        binding.buttonRegisterAccountOptions.setOnClickListener {
            findNavController().apply {
                popBackStack(R.id.splashFragment, false)
                      navigate(R.id.signUpFragment) // Navigate to LoginFragment
            }
        }
    }
    private suspend fun getUserInfo(): DataSnapshot = suspendCoroutine { continuation ->
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val dbref = FirebaseDatabase.getInstance().getReference("Users")
            dbref.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    continuation.resume(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                    continuation.resumeWithException(error.toException())
                }
            })
        } else {
            // Handle the case where the user is not authenticated
            continuation.resumeWithException(Exception("User not authenticated"))
        }
    }
    private fun checkUser() {
        progressDialog.show() // Show progress dialog when checking user
        GlobalScope.launch(Dispatchers.Main) {
            if (isNetworkAvailable()) {
                val firebaseUser = auth.currentUser
                if (firebaseUser == null) {
                    progressDialog.dismiss() // Dismiss progress dialog if user is not logged in
                    findNavController().navigate(R.id.action_splashFragment_to_signInFragment)
                } else {
                    try {
                        val snapshot = getUserInfo()
                        handleUserInfo(snapshot)
                        progressDialog.dismiss()
                    } catch (e: Exception) {
                        progressDialog.dismiss() // Dismiss progress dialog if there's an error
                        showToast("Error fetching user information")
                        e.printStackTrace()
                    }
                }
            } else {
                progressDialog.dismiss() // Dismiss progress dialog if there's no internet
                showNoInternetDialog()
            }
        }
    }


    private fun handleUserInfo(snapshot: DataSnapshot) {
        val userType = snapshot.child("userType").value
        val access = snapshot.child("access").value

        when {
            userType == "admin" -> {
                showToast("Login Successfully")
                findNavController().apply {
                    popBackStack(R.id.splashFragment, false)
                    navigate(R.id.homeNavFragment)
                }
            }
            userType == "member" -> {
                if (access == false) {
                    showReviewDialog()
                    showToast("Your account may have a problem. Please contact the admin for assistance.")
                    auth.signOut()
                    activity?.finish()
                } else if (access == true) {
                    findNavController().apply {
                        popBackStack(R.id.splashFragment, false)
                        navigate(R.id.navUserFragment)
                    }
                }
            }
            else -> {
                showToast("Your account may have a problem. Please contact the admin for assistance.")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    private fun showReviewDialog() {
        val dialogBinding = DialogReviewBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)
        dialog.show()
    }
    @SuppressLint("ServiceCast")
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun showNoInternetDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("No Internet Connection")
            .setMessage("Please connect to the internet to continue.")

            .setNegativeButton("Retry") { _, _ ->

                checkUser()
            }
            .setCancelable(false)
            .show()
    }
}