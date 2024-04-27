package com.example.bidnshare.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bidnshare.databinding.ItemBiddersBinding
import com.example.bidnshare.models.BidModels
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.ocpsoft.prettytime.PrettyTime
import java.util.Date
import java.util.Locale

class BidItemAdapter(private val context: Context, private val bidItems: List<BidModels>) :
    RecyclerView.Adapter<BidItemAdapter.BidItemViewHolder>() {

    inner class BidItemViewHolder(private val binding: ItemBiddersBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BidModels) {
            binding.tvName.text = item.name
            binding.tvBidAmount.text = "₱ ${item.bidPrice}"
            binding.tvMins.text = getTimeAgo(item.minsAgo.toLong())
            Picasso.get().load(item.imageUrl).into(binding.imgPicture)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BidItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBiddersBinding.inflate(inflater, parent, false)
        return BidItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BidItemViewHolder, position: Int) {
        holder.bind(bidItems[position])
    }

    override fun getItemCount(): Int {
        return bidItems.size
    }
    private fun getTimeAgo(timestamp: Long): String {
        val prettyTime = PrettyTime(Locale.getDefault())
        return prettyTime.format(Date(timestamp))
    }
}