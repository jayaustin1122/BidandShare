package com.example.bidnshare.user.details
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.bidnshare.databinding.ItemRowImagesDetailsBinding

class ImageAdapter2(private var imageUrls: List<String?>) : RecyclerView.Adapter<ImageAdapter2.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemRowImagesDetailsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        imageUrl?.let { holder.bind(it) } // Only bind non-null URLs
    }

    override fun getItemCount(): Int {
        return imageUrls.count { it != null } // Count non-null URLs
    }

    inner class ImageViewHolder(private val binding: ItemRowImagesDetailsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(imageUrl: String) {
            // Load image into ImageView using Glide
            Glide.with(binding.root.context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.imgBookCover)
        }
    }

    fun setImageUrls(imageUrls: List<String?>) {
        this.imageUrls = imageUrls
        notifyDataSetChanged() // Notify adapter that data set has changed
    }
}
