package com.example.mdcs.user.tabs.messenger

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bidnshare.R
import com.example.bidnshare.databinding.FragmentChatUserBinding
import com.example.bidnshare.notification.NotificationData
import com.example.bidnshare.notification.PushNotification
import com.example.bidnshare.notification.RetrofitInstance
import com.example.bidnshare.user.chat.ChatModel
import com.example.bidnshare.user.chat.ChatUserAdapter
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ChatUserFragment : Fragment() {
    private lateinit var binding : FragmentChatUserBinding
    private val database = Firebase.database
    private val ref = database.getReference("Users")
    var topic = ""
    var chatList = ArrayList<ChatModel>()
    var firebaseUser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatUserBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userID = arguments?.getString("userID")
        val token = arguments?.getString("token")
        val fullName = arguments?.getString("fullName")
        firebaseUser = Firebase.auth.currentUser
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        LoadUserInfo(userID)
        binding.imgBack.setOnClickListener {
            findNavController().apply {
                popBackStack(R.id.chatUserFragment, false) // Pop all fragments up to HomeFragment
                navigate(R.id.homeUserFragment) // Navigate to LoginFragment
            }
        }
        binding.btnSendMessage.setOnClickListener {
            var message: String = binding.etMessages.text.toString()

            if (message.isEmpty()) {
                Toast.makeText(requireContext(), "message is empty", Toast.LENGTH_SHORT).show()
                binding.etMessages.setText("")
            } else {

                binding.etMessages.setText("")
                topic = token.toString()
                PushNotification(
                    NotificationData("New Message", binding.etMessages.text.toString()),
                    topic
                ).also {
                    sendNotification(it)
                }
                sendMessage(firebaseUser!!.uid, userID!!, message)
            }
        }

        if (userID != null) {
            firebaseUser?.let { readMessage(it.uid, userID) }
        }
    }
    private fun onBackButtonPressed() {
        // Navigate back to the previous fragment
        findNavController().popBackStack()
    }
    private fun sendMessage(senderId: String, receiverId: String, message: String) {
        var reference: DatabaseReference? = FirebaseDatabase.getInstance().getReference()

        var hashMap: HashMap<String, String> = HashMap()
        hashMap.put("senderId", senderId)
        hashMap.put("receiverId", receiverId)
        hashMap.put("message", message)

        reference!!.child("Chat").push().setValue(hashMap)

    }
    fun readMessage(senderId: String, receiverId: String) {
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Chat")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val chat = dataSnapShot.getValue(ChatModel::class.java)

                    if (chat!!.senderId.equals(senderId) && chat!!.receiverId.equals(receiverId) ||
                        chat!!.senderId.equals(receiverId) && chat!!.receiverId.equals(senderId)
                    ) {
                        chatList.add(chat)
                    }
                }

                val chatAdapter = ChatUserAdapter(this@ChatUserFragment, chatList)

                binding.chatRecyclerView.adapter = chatAdapter
            }
        })
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


    fun LoadUserInfo(userID: String?) {
    if (userID != null) {
        ref.child(userID).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val name = dataSnapshot.child("fullName").getValue(String::class.java)
                val image = dataSnapshot.child("image").getValue(String::class.java)



                binding.tvUserName.text = name
                Glide.with(this@ChatUserFragment)
                    .load(image)
                    .into(binding.imgProfile)


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error case
                Log.w(ContentValues.TAG, "loadAllInfo:onCancelled", databaseError.toException())
            }
        })
    }
}

}
