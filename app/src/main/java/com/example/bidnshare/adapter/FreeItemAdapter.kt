package com.example.bidnshare.adapter

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bidnshare.databinding.ItemRowLiveAuctionBinding
import com.example.bidnshare.databinding.ItemRowLiveFreeItemsBinding
import com.example.bidnshare.models.FreeItems
import com.example.bidnshare.models.sellItem

class FreeItemAdapter(private val context: Context,
                      private var eventArrayList: ArrayList<FreeItems>
): RecyclerView.Adapter<FreeItemAdapter.ViewHolder>(){
    private  lateinit var binding: ItemRowLiveFreeItemsBinding
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = binding.tvTitle
        var moreBtn: ImageView = binding.imgBookCover
        var price: TextView = binding.away
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemRowLiveFreeItemsBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //get data
        val model = eventArrayList[position]
        val price = model.price
        val name = model.title
        val imageselected = model.imageResourceId


        //set data's
        holder.title.text = name
        holder.price.text = price
        Glide.with(this@FreeItemAdapter.context)
            .load(imageselected)
            .into(holder.moreBtn)



    }

    override fun getItemCount(): Int {
        return eventArrayList.size
    }

}