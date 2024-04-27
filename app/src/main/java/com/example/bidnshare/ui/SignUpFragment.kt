package com.example.bidnshare.ui

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.bidnshare.R
import com.example.bidnshare.adapter.ViewPagerAdapter
import com.example.bidnshare.databinding.FragmentSignUpBinding
import com.example.bidnshare.ui.signup.FiveSignupFragment
import com.example.bidnshare.ui.signup.FourSignupFragment
import com.example.bidnshare.ui.signup.OneSignupFragment
import com.example.bidnshare.ui.signup.SignUpViewModel
import com.example.bidnshare.ui.signup.ThreeSignupFragment
import com.example.bidnshare.ui.signup.TwoSignupFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.shuhart.stepview.StepView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone


open class SignUpFragment : Fragment() {
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: ViewPagerAdapter
    private lateinit var stepView: StepView
    private lateinit var viewModel: SignUpViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog : ProgressDialog
    private lateinit var storage : FirebaseStorage
    private lateinit var database : FirebaseDatabase
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        viewPager = binding.viewpagersignup
        stepView = binding.stepView
        return binding.root
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(SignUpViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ViewPagerAdapter(requireActivity())
        viewPager.adapter = adapter
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance()
        progressDialog = ProgressDialog(this.requireContext())
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        // Initialize fragments and add them to the adapter
        adapter.addFragment(OneSignupFragment())
        adapter.addFragment(TwoSignupFragment())
        adapter.addFragment(ThreeSignupFragment())
        adapter.addFragment(FourSignupFragment())
        adapter.addFragment(FiveSignupFragment())

        // Set initial step count for StepView
        stepView.go(0, true)
        val stepLabels = listOf("Personal", "Profile", "Birthday", "Gender", "Complete")
        stepView.setSteps(stepLabels)
        // Set listener for ViewPager changes to update StepView
        viewPager.isUserInputEnabled = false
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                stepView.go(position, true)
            }
        })
        binding.btnContinue.setOnClickListener {
            when (viewPager.currentItem) {
                0 -> validateFragmentOne()
                1 -> validateFragmentTwo()
                2 -> validateFragmentThree()
                3 -> validateFragmentFour()
            }
        }


    }


    fun nextItem(){
        val currentItem = viewPager.currentItem
        val nextItem = currentItem + 1
        if (nextItem < adapter.itemCount) {
            viewPager.currentItem = nextItem

        }
    }
    fun validateFragmentOne(){
        val fullname = viewModel.fullname
        val email = viewModel.email
        val password = viewModel.password
        if (fullname.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your fullname", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show()
        } else if (password.length < 8) {
            Toast.makeText(requireContext(), "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show()
        } else {
            nextItem()
        }
    }
    // Inside SignUpFragment
    fun validateFragmentTwo() {
        val selectedImageUri = viewModel.image // Access the image URI from the view model

        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "Please upload a profile picture", Toast.LENGTH_SHORT).show()
            Log.d("SignUpFragment", "validateFragmentTwo: selectedImageUri is null")
        } else {
            nextItem()
            Log.d("SignUpFragment", "validateFragmentTwo: selectedImageUri is not null")
        }
    }
    fun validateFragmentFour() {
        val selectedImageUri = viewModel.gender // Access the image URI from the view model
        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "Please Select Gender", Toast.LENGTH_SHORT).show()

        } else {
            Handler().postDelayed({
                progressDialog.dismiss()
                createUserAccount()
                nextItem()
                progressDialog.setMessage("Creating Account...")
                progressDialog.show()
            }, 5000)


        }
    }
    fun validateFragmentThree(){

        val month = viewModel.month
        val day = viewModel.day
        val year = viewModel.year
        if (month.isEmpty()||day.isEmpty()||year.isEmpty()) {
            Toast.makeText(requireContext(), "Empty Fields are not allowed", Toast.LENGTH_SHORT).show()
        }  else {
            nextItem()
        }
    }
    private fun createUserAccount() {
        progressDialog.setMessage("Creating Account...")
        progressDialog.show()
        val email = viewModel.email
        val password = viewModel.password
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()

                // Get the FCM token
                val fcmToken = FirebaseMessaging.getInstance().token.await()

                withContext(Dispatchers.Main) {
                    uploadImage(fcmToken)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@SignUpFragment.requireContext(),
                        "Failed Creating Account or ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    private fun uploadImage( token: String) {
        progressDialog.setMessage("Uploading Image...")
        progressDialog.show()

        val reference = storage.reference.child("profile")
            .child(token!!)
        viewModel.image?.let {
            reference.putFile(it).addOnCompleteListener {
                if (it.isSuccessful) {
                    reference.downloadUrl.addOnSuccessListener { image ->
                        uploadToFirebase(token, image.toString())
                    }
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@SignUpFragment.requireContext(),
                        "Error uploading image",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    private fun uploadToFirebase(token: String?, imageUrl: String){
        progressDialog.setMessage("Saving Account...")
        progressDialog.show()
        val fullname = viewModel.fullname
        val email = viewModel.email
        val password = viewModel.password
        val month = viewModel.month
        val day = viewModel.day
        val year = viewModel.year
        val gender = viewModel.gender
        val selectedImageUri = viewModel.image
        val currentDate = getCurrentDate()
        val currentTime = getCurrentTime()
        val uid2 = auth.uid
        val timestamp = System.currentTimeMillis()


        val hashMap : HashMap<String, Any?> = HashMap()

        hashMap["uid"] = uid2
        hashMap["email"] = email
        hashMap["password"] = password
        hashMap["fullName"] = fullname
        hashMap["image"] = imageUrl
        hashMap["currentDate"] = currentDate
        hashMap["currentTime"] = currentTime
        hashMap["gender"] = gender
        hashMap["phone"] = ""
        hashMap["userType"] = "member"
        hashMap["access"] = false
        hashMap["token"] = token
        hashMap["dateOfBirth"] = "$month-$day-$year"
        hashMap["timestamp"] = timestamp
        hashMap["address"] = "----"
        try {
            database.getReference("Users")
                .child(uid2!!)
                .setValue(hashMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        progressDialog.dismiss()
                        findNavController().apply {
                            popBackStack(R.id.signUpFragment, false)
                            navigate(R.id.signInFragment)
                        }
                        Toast.makeText(
                            this@SignUpFragment.requireContext(),
                            "Account Created",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        progressDialog.dismiss()
                        Toast.makeText(
                            this@SignUpFragment.requireContext(),
                            task.exception!!.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } catch (e: Exception) {
            progressDialog.dismiss()
            Toast.makeText(
                this.requireContext(),
                "Error uploading data: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun getCurrentTime(): String {
        val tz = TimeZone.getTimeZone("GMT+08:00")
        val c = Calendar.getInstance(tz)
        val hours = String.format("%02d", c.get(Calendar.HOUR))
        val minutes = String.format("%02d", c.get(Calendar.MINUTE))
        return "$hours:$minutes"
    }


    @SuppressLint("SimpleDateFormat")
    private fun getCurrentDate(): String {
        val currentDateObject = Date()
        val formatter = SimpleDateFormat(   "dd-MM-yyyy")
        return formatter.format(currentDateObject)
    }
}



