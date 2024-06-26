package com.example.bidnshare.adapter
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bidnshare.R
import com.example.bidnshare.databinding.ItemRowPendingBinding
import com.example.bidnshare.models.PendingItem
import com.example.bidnshare.user.details.DetailsFragment
import com.example.bidnshare.user.profile.detailsItem.AllDetailsFragment

class PendingAdapter(
    private val context: Context,
    private val navController: NavController,
    private val items: MutableList<PendingItem>,
    private val emptyListener: (Any) -> Unit
) :
    RecyclerView.Adapter<PendingAdapter.PendingViewHolder>() {

    private var selectedItems: MutableSet<PendingItem> = mutableSetOf()
    private var totalPrice: Double = 0.0
    private lateinit var totalPriceTextView: TextView
    private lateinit var payButton: Button

    fun setTotalPriceTextView(textView: TextView) {
        totalPriceTextView = textView
        updateTotalPriceTextView()
    }

    fun setPayButton(button: Button) {
        payButton = button
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ItemRowPendingBinding.inflate(inflater, parent, false)
        return PendingViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: PendingViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        val timeStamp = item.timeStamp
        val uid = item.uid
        val imageselected = item.imageUrl

        holder.itemView.setOnClickListener {
            val detailsFragment = AllDetailsFragment()
            val bundle = Bundle()
            bundle.putString("timeStamp", timeStamp)
            bundle.putString("uid", uid)
            bundle.putString("image", imageselected)
            detailsFragment.arguments = bundle
            navController.navigate(R.id.allDetailsFragment, bundle)
        }

        holder.binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedItems.add(item)
            } else {
                selectedItems.remove(item)
            }
            updateTotalPriceTextView()
            checkPayButtonEnable()
            emptyListener.invoke(selectedItems.isEmpty())
        }
    }

    override fun getItemCount(): Int {
        val isEmpty = items.isEmpty()
        emptyListener.invoke(isEmpty)
        return items.size
    }



    inner class PendingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemRowPendingBinding.bind(itemView)

        fun bind(item: PendingItem) {
            binding.title.text = item.title
            binding.tvPrice.text = item.price
            binding.checkbox.isChecked = selectedItems.contains(item)
            // Load image using Glide
            Glide.with(context)
                .load(item.imageUrl)
                .into(binding.img)
        }
    }

    private fun updateTotalPriceTextView() {
        totalPrice = selectedItems.sumByDouble { it.price.toDouble() }
        totalPriceTextView.text = totalPrice.toString()
    }

    private fun checkPayButtonEnable() {
        payButton.isEnabled = selectedItems.isNotEmpty()
    }
    fun setItems(items: List<PendingItem>) {
        this.items.clear()
        this.items.addAll(items)
    }
    fun getSelectedItem(): PendingItem? {
        // Return the first selected item, or null if no item is selected
        return selectedItems.firstOrNull()
    }
}

