package com.example.bidnshare.adapter

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bidnshare.R
import com.example.bidnshare.admin.DetailsAdminFragment
import com.example.bidnshare.databinding.ItemRowLiveAuctionAdminBinding
import com.example.bidnshare.models.sellItem
import com.example.bidnshare.models.sellItemAdmin
import com.example.bidnshare.user.details.DetailsFragment
import com.google.firebase.database.FirebaseDatabase

class LiveAuctionAdapterAdmin(private val context: Context,
                              private val navController: NavController,
                              private var eventArrayList: List<sellItemAdmin>
): RecyclerView.Adapter<LiveAuctionAdapterAdmin.ViewHolder>(){
    private  lateinit var binding: ItemRowLiveAuctionAdminBinding

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = binding.tvTitle
        var moreBtn: ImageView = binding.imgBookCover
        var price: TextView = binding.price
        var time: TextView = binding.time
        var options: ImageView = binding.morebtn
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemRowLiveAuctionAdminBinding.inflate(LayoutInflater.from(context), parent, false)
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
        val type = model.type
        val sellOrFree = model.sellOrFree


        //set data's
        holder.title.text = name
        holder.price.text = price
        holder.time.text = type
        Glide.with(this@LiveAuctionAdapterAdmin.context)
            .load(imageselected)
            .into(holder.moreBtn)
        holder.itemView.setOnClickListener {
            val detailsFragment = DetailsAdminFragment()
            val bundle = Bundle()
            bundle.putString("timeStamp", timeStamp)
            bundle.putString("uid", uid)
            bundle.putString("image", imageselected)
            bundle.putString("type", type)
            bundle.putString("sellOrFree", sellOrFree)
            detailsFragment.arguments = bundle
            navController.navigate(R.id.detailsAdminFragment, bundle)
        }
        holder.options.setOnClickListener {
            val access = model.type
            val uid = model.uid

            val options = arrayOf("Add to Newly", "Add to Upcoming")
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Choose Option")
                .setItems(options) { dialog, position ->
                    //handle item clicked
                    if (position == 0) {
                        updateToNewly(model)
                    } else if (position == 1) {
                        updateToUpcoming(model)
                    }
                }
                .show()
        }


    }

    private fun updateToUpcoming(model: sellItemAdmin) {

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Update?")
            .setMessage("Are you sure you want to update this Item?")
            .setPositiveButton("Confirm") { a, d ->

                updateInDb(model)
                Toast.makeText(context, "Item Updated", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { a, d ->
                a.dismiss()
            }
            .show()
    }

    private fun updateInDb(model: sellItemAdmin) {
// Get a reference to the sell item in the database
        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(model.uid).child("mySellItems").child(model.timeStamp)

        // Update the type field in the database
        dbRef.child("type").setValue("Upcoming")
            .addOnSuccessListener {
                // Handle successful update
                Toast.makeText(context, "Item Updated in Database", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Handle failed update
                Toast.makeText(context, "Failed to update item: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateToNewly(model: sellItemAdmin) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Update?")
            .setMessage("Are you sure you want to update this Item?")
            .setPositiveButton("Confirm") { a, d ->

                updateInDbNewly(model)
                Toast.makeText(context, "Item Updated", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { a, d ->
                a.dismiss()
            }
            .show()
    }

    private fun updateInDbNewly(model: sellItemAdmin) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(model.uid).child("mySellItems").child(model.timeStamp)

        // Update the type field in the database
        dbRef.child("type").setValue("Newly")
            .addOnSuccessListener {
                // Handle successful update
                Toast.makeText(context, "Item Updated in Database", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Handle failed update
                Toast.makeText(context, "Failed to update item: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int {
        return eventArrayList.size
    }

}