package com.example.bidnshare.service

import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


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

            }
        }
        countdownTimer.start()
        return START_NOT_STICKY
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
