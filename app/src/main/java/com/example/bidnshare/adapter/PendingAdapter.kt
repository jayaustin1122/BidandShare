package com.example.bidnshare.adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bidnshare.databinding.ItemRowPendingBinding
import com.example.bidnshare.models.PendingItem

class PendingAdapter(private val context: Context, private val items: MutableList<PendingItem>) :
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

        holder.itemView.setOnClickListener {
            //details fragment
        }

        holder.binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedItems.add(item)
            } else {
                selectedItems.remove(item)
            }
            updateTotalPriceTextView()
            checkPayButtonEnable()
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class PendingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemRowPendingBinding.bind(itemView)

        fun bind(item: PendingItem) {
            binding.title.text = item.title
            binding.tvPrice.text = item.price
            binding.checkbox.isChecked = selectedItems.contains(item)
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
}

