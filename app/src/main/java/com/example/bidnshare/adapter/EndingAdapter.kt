package com.example.bidnshare.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bidnshare.databinding.ItemRowAlllistBinding
import com.example.bidnshare.databinding.ItemRowLiveAuctionBinding
import com.example.bidnshare.models.EndingItems
import com.example.bidnshare.models.FreeItems
import com.example.bidnshare.models.NewlyItems
import com.example.bidnshare.models.UpcomingItems

class EndingAdapter(private val context: Context,
                    private var endingArrayList: ArrayList<EndingItems>
): RecyclerView.Adapter<EndingAdapter.ViewHolder>(){
    private  lateinit var binding: ItemRowAlllistBinding
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = binding.tvTitle
        var moreBtn: ImageView = binding.imgBookCover
        var price: TextView = binding.price
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EndingAdapter.ViewHolder {
        binding = ItemRowAlllistBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: EndingAdapter.ViewHolder, position: Int) {
        //get data
        val model = endingArrayList[position]
        val price = model.price.toString()
        val name = model.title
        val imageselected = model.imageResourceId


        //set data's
        holder.title.text = name
        holder.price.text = price
        Glide.with(this@EndingAdapter.context)
            .load(imageselected)
            .into(holder.moreBtn)



    }

    override fun getItemCount(): Int {
        return endingArrayList.size
    }


}