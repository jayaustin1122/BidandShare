package com.example.bidnshare.adapter

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bidnshare.R
import com.example.bidnshare.databinding.ItemRowLiveAuctionBinding
import com.example.bidnshare.models.sellItem
import com.example.bidnshare.user.details.DetailsFragment

class LiveAuctionAdapter(private val context: Context,
                         private val navController: NavController,
                         private var eventArrayList: ArrayList<sellItem>
): RecyclerView.Adapter<LiveAuctionAdapter.ViewHolder>(){
    private  lateinit var binding: ItemRowLiveAuctionBinding
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = binding.tvTitle
        var moreBtn: ImageView = binding.imgBookCover
        var price: TextView = binding.price
        var time: TextView = binding.time
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemRowLiveAuctionBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //get data
        val model = eventArrayList[position]
        val price = model.price.toString()
        val name = model.title
        val imageselected = model.imageResourceId
        val time = model.time
        val timeStamp = model.timeStamp
        val uid = model.uid


        //set data's
        holder.title.text = name
        holder.price.text = price
        holder.time.text = time
        Glide.with(this@LiveAuctionAdapter.context)
            .load(imageselected)
            .into(holder.moreBtn)
        holder.itemView.setOnClickListener {
            val detailsFragment = DetailsFragment()
            val bundle = Bundle()
            bundle.putString("timeStamp", timeStamp)
            bundle.putString("uid", uid)
            bundle.putString("image", imageselected)
            detailsFragment.arguments = bundle
            navController.navigate(R.id.detailsFragment, bundle)
        }


    }

    override fun getItemCount(): Int {
        return eventArrayList.size
    }

}