package com.example.bidnshare.user

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bidnshare.R
import com.example.bidnshare.adapter.ImageAdapter
import com.example.bidnshare.databinding.FragmentAddNewItemBinding
import com.example.bidnshare.notification.NotificationData
import com.example.bidnshare.notification.PushNotification
import com.example.bidnshare.notification.RetrofitInstance
import com.example.bidnshare.user.viewmodels.AddViewModel
import com.example.bidnshare.util.Inits
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
const val TOPIC = "/topics/myTopic2"
class AddNewItemFragment : BottomSheetDialogFragment() {
    private lateinit var binding : FragmentAddNewItemBinding
    private lateinit var inits: Inits
    private lateinit var selectedImage: Uri
    private lateinit var viewModel: AddViewModel
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var sellOrFreeSpinner: Spinner
    private val selectedImagesList = mutableListOf<Uri>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddNewItemBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(AddViewModel::class.java)
        val images: MutableList<Uri> = viewModel.imageList.value?.toMutableList() ?: mutableListOf()
        imageAdapter = ImageAdapter(images)
        // Observe image list changes
        viewModel.imageList.observe(viewLifecycleOwner) { images ->
            imageAdapter.updateImages(images)
        }

        binding.recyclerimages.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = imageAdapter
        }

        return binding.root
    }
    private fun addImagesToViewModel(uris: List<Uri>) {
        viewModel.addImages(uris)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inits = Inits(requireContext())
        inits.initialize()
        sellOrFreeSpinner = view.findViewById(R.id.spinnerSellOrFree)

        // Set up the spinner adapter
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.sell_or_free_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sellOrFreeSpinner.adapter = adapter
        }
        binding.materialButton.setOnClickListener {
            launchImageSelection()
        }

        binding.btnAddItem.setOnClickListener {
            validateData()

        }
    }

    private fun launchImageSelection() {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // Enable multiple image selection
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGES)

    }

    private fun validateData() {
        val name = binding.etNameOfCrop.text.toString().trim()
        val price = binding.etDetails.text.toString().trim()
        val detail = binding.etprice.text.toString().trim()
        val sellOrFree = sellOrFreeSpinner.selectedItem.toString()


        when {

            name.isEmpty() ->  Snackbar.make(binding.root, "Please Enter Item Name", Snackbar.LENGTH_SHORT).show()
            detail.isEmpty() -> Snackbar.make(binding.root, "Please Enter $name Details...", Snackbar.LENGTH_SHORT).show()
            price.isEmpty() -> Snackbar.make(binding.root, "Please Add Amount", Snackbar.LENGTH_SHORT).show()
            !::selectedImage.isInitialized -> Snackbar.make(binding.root, "Please Select Image...", Snackbar.LENGTH_SHORT).show()
            else -> uploadImages(selectedImagesList,sellOrFree)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                if (data.data != null) {
                    // Handle single image selection
                    selectedImage = data.data!!
                    binding.imageselect.setImageURI(selectedImage)
                    selectedImagesList.add(selectedImage) // Add selected image to the list
                } else if (data.clipData != null) {
                    // Handle multiple image selection
                    val clipData = data.clipData!!
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        selectedImagesList.add(uri) // Add selected image to the list
                    }
                    addImagesToViewModel(selectedImagesList) // Add selected images to ViewModel

                }
            }
        }
    }
    private fun uploadImages(imagesList: List<Uri>, sellOrFree: String) {
        val auth = inits.getAuth()
        val database = inits.getDatabase()
        val storage = inits.getStorage()
        val progressDialog = inits.getProgressDialog()

        progressDialog.setMessage("Uploading Images...")
        progressDialog.show()

        val uid = auth.uid
        val timestamp = System.currentTimeMillis()
        val uploadTasks = mutableListOf<Task<Uri>>()

        // List to store the URLs of the uploaded images
        val uploadedImageUrls = mutableListOf<String>()

        // Upload each image in the list
        imagesList.forEachIndexed { index, uri ->
            val reference = storage.reference.child("images/$timestamp-$index.jpg")
                .child(uid!!)
            val uploadTask = reference.putFile(uri)

            // Add the continuation task to properly handle the result
            val continuationTask = uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                reference.downloadUrl
            }
            // Add the continuation task to the list of tasks
            uploadTasks.add(continuationTask)
        }

        // Wait for all uploads to complete
        Tasks.whenAllSuccess<Uri>(uploadTasks)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrls = task.result as List<Uri>

                    // Store the URLs of the uploaded images
                    downloadUrls.forEach { url ->
                        uploadedImageUrls.add(url.toString())
                    }

                    // Upload info with the URLs of the uploaded images
                    uploadInfo(uploadedImageUrls, sellOrFree)

                    progressDialog.dismiss()
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@AddNewItemFragment.requireContext(),
                        "Error uploading images",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }






    companion object {
        private const val REQUEST_CODE_PICK_SINGLE_IMAGE = 101
        private const val REQUEST_CODE_PICK_IMAGES = 100
    }

    fun uploadInfo(imageURIs: MutableList<String>, sellOrFree: String) {
        val auth = inits.getAuth()
        val database = inits.getDatabase()
        val progressDialog = inits.getProgressDialog()

        progressDialog.setMessage("Saving Data...")
        progressDialog.show()

        // Retrieve other item details
        val name = binding.etNameOfCrop.text.toString().trim()
        val details = binding.etDetails.text.toString().trim()
        val price = binding.etprice.text.toString().trim()
        val uid = auth.uid
        val timestamp = System.currentTimeMillis()

        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["ItemId"] = uid
        hashMap["ItemName"] = name
        hashMap["ItemDetails"] = details
        hashMap["price"] = price
        hashMap["currentDate"] = getCurrentDate()
        hashMap["currentTime"] = getCurrentTime()
        hashMap["timestamp"] = timestamp.toString()
        hashMap["currentUser"] = uid
        hashMap["sellOrFree"] = sellOrFree
        hashMap["type"] = ""
        hashMap["timer"] = ""

        // Store URIs as strings in a list
        val imageURIsAsString = imageURIs.map { it.toString() }
        hashMap["imageURIs"] = imageURIsAsString
        try {
            database.getReference("Users")
                .child(uid!!)
                .child("mySellItems")
                .child(timestamp.toString()) // Unique identifier for each item
                .updateChildren(hashMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        progressDialog.dismiss()
                        findNavController().apply {
                            PushNotification(
                                NotificationData("MDCS", "Admin Added a new Data in Crops"),
                                TOPIC
                            ).also {
                                sendNotification(it)
                            }
                        }
                        dismiss()
                        Toast.makeText(
                            requireContext(),
                            "Data Saved!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            task.exception!!.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } catch (e: Exception) {
            progressDialog.dismiss()
            Toast.makeText(
                requireContext(),
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
    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful) {
                Log.d(ContentValues.TAG, "Notification sent successfully")
            } else {
                Log.e(ContentValues.TAG, "Failed to send notification. Error: ${response.errorBody().toString()}")
            }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error sending notification: ${e.toString()}")
        }
    }
}