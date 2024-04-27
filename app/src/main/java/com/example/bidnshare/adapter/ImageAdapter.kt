package com.example.bidnshare.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bidnshare.databinding.ItemRowImagesBinding
import com.squareup.picasso.Picasso

class ImageAdapter(private var images: MutableList<Uri>) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemRowImagesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUri = images[position]
        holder.bind(imageUri)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    fun updateImages(newImages: List<Uri>) {
        images.clear()
        images.addAll(newImages)
        notifyDataSetChanged()
    }

    inner class ImageViewHolder(private val binding: ItemRowImagesBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imageUri: Uri) {
            Picasso.get().load(imageUri).into(binding.imgBookCover)
            binding.btnRemoveImage.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    images.removeAt(position)
                    notifyItemRemoved(position)
                }
            }
        }
    }
}
