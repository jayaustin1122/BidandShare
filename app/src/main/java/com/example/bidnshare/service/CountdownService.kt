package com.example.bidnshare.service

import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.bidnshare.MainActivity
import com.example.bidnshare.R
import com.example.bidnshare.notification.NotificationData
import com.example.bidnshare.notification.PushNotification
import com.example.bidnshare.notification.RetrofitInstance
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CountdownService : Service() {

    private var timer: CountDownTimer = object : CountDownTimer(0, 0) {
        override fun onTick(millisUntilFinished: Long) {}
        override fun onFinish() {}
    }
    private val db = FirebaseDatabase.getInstance().reference

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification() // Create notification
        startForeground(NOTIFICATION_ID, notification)

        val dbPath = intent?.getStringExtra("dbPath")
        val durationMinutes = intent?.getIntExtra("durationMinutes", 0) ?: 0
        val auctionEndTimeMillis = System.currentTimeMillis() + (durationMinutes * 60 * 1000)
        val timeStamp = intent?.getStringExtra("timeStamp")
        val uid =intent?.getStringExtra("uid")
        Log.d("Sample", "uid $uid Timestamp $timeStamp")
        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(uid!!).child("mySellItems").child(timeStamp!!)
        val countdownTimer = object : CountDownTimer(auctionEndTimeMillis - System.currentTimeMillis(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d("CountdownService", "onTick() called")
                // Update database with remaining time
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                val timerMap = mapOf("minutes" to minutes, "seconds" to seconds)
                db.child("Users").child(uid!!).child("mySellItems")
                    .child(timeStamp!!)
                    .child("countdownTimer")
                    .setValue(timerMap)

                val intent = Intent("countdown_update")
                intent.putExtra("remainingTime", millisUntilFinished)
                sendBroadcast(intent)
            }

            override fun onFinish() {
                retrieveHighestBidder(dbRef)
            }
        }
        countdownTimer.start()
        return START_NOT_STICKY
    }
    private fun retrieveHighestBidder(dbRef: DatabaseReference) {
        dbRef.child("Bids")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(bidsSnapshot: DataSnapshot) {
                    var highestBidPriceString: String? = null
                    var highestBidderUid: String? = null

                    for (bidSnapshot in bidsSnapshot.children) {
                        val bidPriceString = bidSnapshot.child("bidPrice").getValue(String::class.java)
                        val bidderId = bidSnapshot.child("userBid").getValue(String::class.java)

                        if (bidPriceString != null) {
                            val bidPrice = bidPriceString.toDoubleOrNull() ?: 0.0
                            if (bidPrice > (highestBidPriceString?.toDoubleOrNull() ?: 0.0)) {
                                highestBidPriceString = bidPriceString
                                highestBidderUid = bidderId
                            }
                        }
                    }

                    if (highestBidderUid != null) {
                        // If the highest bidder's ID is found, pass it to retrieveBidderInfo function.
                        retrieveBidderInfo(highestBidderUid,dbRef)
                    } else {
                        // Handle case where there are no bids.
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }



    private fun retrieveBidderInfo(uid: String?, dbRef: DatabaseReference) {
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid!!)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bidderName = snapshot.child("fullName").getValue(String::class.java)
                val uid = snapshot.child("uid").getValue(String::class.java)
                val token = snapshot.child("token").getValue(String::class.java)

                updateSellOrFree(dbRef,uid)
                val topic = token.toString()
                PushNotification(
                    NotificationData("Congrats", "You Are the Winner for the Auction of an Item"),
                    topic
                ).also {
                    sendNotification(it)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
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

    private fun updateSellOrFree(dbRef: DatabaseReference, uid: String?) {
        dbRef.child("sellOrFree").setValue("Ordered")
            .addOnSuccessListener {
                // If the first update is successful, update the second value
                dbRef.child("type").setValue("Ending")
                    .addOnSuccessListener {
                        // Handle successful update of both values
                        dbRef.child("orderedBy").setValue(uid)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Item Transfer to the Client",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                            .addOnFailureListener { e ->
                                // Handle failure of the second update
                                Toast.makeText(
                                    this,
                                    "Failed to update item type: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
            }
            .addOnFailureListener { e ->
                // Handle failure of the first update
                Toast.makeText(this, "Failed to update item sellOrFree: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val NOTIFICATION_ID = 123 // Unique ID for the notification
    }
    private fun createNotification(): Notification {
        val channelId = "CountdownServiceChannel"
        val channelName = "Countdown Service Channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Countdown Service")
            .setContentText("Running...")
            .setSmallIcon(R.drawable.icon_background)
            .setContentIntent(pendingIntent)
            .build()
    }







}
