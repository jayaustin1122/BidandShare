package com.example.bidnshare

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import com.example.bidnshare.user.details.BidDialogFragment

class MainActivity : AppCompatActivity() , BidDialogFragment.OnBidAddedListener{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onBidAdded(userHasBid: Boolean) {

    }
}