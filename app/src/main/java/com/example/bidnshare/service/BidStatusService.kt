package com.example.bidnshare.service

import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.google.firebase.database.*

class BidStatusService : Service() {
    private lateinit var database: DatabaseReference
    private lateinit var wakeLock: PowerManager.WakeLock

    override fun onCreate() {
        super.onCreate()
        database = FirebaseDatabase.getInstance().reference

        // Acquire a wake lock to keep the CPU running
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BidStatusService::WakeLock")
        wakeLock.acquire()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val uid = intent?.getStringExtra("uid")
        val timeStamp = intent?.getStringExtra("timeStamp")
        startBidStatusListener(uid, timeStamp)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
        Log.d(TAG, "onDestroy")
    }

    private fun startBidStatusListener(uid: String?, timeStamp: String?) {
        val userBid = database.child("Users")
            .child(uid!!)
            .child("mySellItems")
            .child(timeStamp.toString())
            .child("Bids")
            .child("bidStatus")
        Log.d(TAG, "Bid Status from Firebase: $uid")
        Log.d(TAG, "Bid Status from Firebase: $timeStamp")
        val bidStatusTimeLeftReference = database.child("Users")
            .child(uid!!)
            .child("mySellItems")
            .child(timeStamp.toString())
            .child("Bids")
            .child("bidStatusTimeLeft")

        userBid.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val bidStatus = dataSnapshot.getValue(Boolean::class.java)
                Log.d(TAG, "Bid Status from Firebase: $bidStatus")  // Log for debugging
                if (bidStatus == true) {
                    Log.d(TAG, "Starting countdown timer")  // Log for debugging
                    // Bid status is active, start countdown timer
                    startCountdownTimer(24 * 60 * 60 * 1000, bidStatusTimeLeftReference) // 24 hours in milliseconds
                } else {
                    // Bid status is inactive, stop the service
                    stopSelf()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, "onCancelled: ${databaseError.toException()}")
            }
        })
    }

    private fun startCountdownTimer(durationMillis: Long, bidStatusTimeLeftReference: DatabaseReference) {
        object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Calculate remaining time in seconds
                val remainingTimeSeconds = millisUntilFinished / 1000

                // Save remaining time to Firebase database
                bidStatusTimeLeftReference.setValue(remainingTimeSeconds)

                val remainingTime = String.format(
                    "%02d:%02d:%02d",
                    millisUntilFinished / (1000 * 60 * 60),
                    (millisUntilFinished / (1000 * 60)) % 60,
                    (millisUntilFinished / 1000) % 60
                )
                Log.d(TAG, "Remaining time: $remainingTime")
            }

            override fun onFinish() {
                // Set remaining time to 0 and save to Firebase database
                bidStatusTimeLeftReference.setValue(0)

                Log.d(TAG, "Countdown timer finished")
            }
        }.start()
    }

    companion object {
        private const val TAG = "BidStatusService"
    }
}



//    val database = FirebaseDatabase.getInstance().reference
//    val userBidRef = database.child("Users").child(uid!!).child("mySellItems").child(timeStamp.toString()).child("Bids").child("bidStatus")
//
//// Listen for changes in bid status
//    userBidRef.addValueEventListener(object : ValueEventListener {
//        override fun onDataChange(dataSnapshot: DataSnapshot) {
//            val bidStatus = dataSnapshot.getValue(Boolean::class.java)
//            if (bidStatus == true) {
//                // Start the service
//                val serviceIntent = Intent(this@YourActivity, BidStatusService::class.java)
//                startService(serviceIntent)
//            } else {
//                // Stop the service
//                val serviceIntent = Intent(this@YourActivity, BidStatusService::class.java)
//                stopService(serviceIntent)
//            }
//        }
//
//        override fun onCancelled(databaseError: DatabaseError) {
//            Log.d(TAG, "onCancelled: ${databaseError.toException()}")
//        }
//    })

