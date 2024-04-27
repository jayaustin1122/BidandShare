package com.example.bidnshare.user.chat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bidnshare.R
import com.example.bidnshare.admin.accounts.AccountsModel
import com.example.bidnshare.databinding.ItemAccountChatBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MessengerUserAdapter(private val context: Context,
                           private var accountArrayList: List<AccountsModel>,
                           private val navController: NavController
):
    RecyclerView.Adapter<MessengerUserAdapter.ViewHolderAccounts>(){
    private lateinit var binding : ItemAccountChatBinding
    private val database = Firebase.database
    inner class ViewHolderAccounts(itemView: View): RecyclerView.ViewHolder(itemView) {
        val name : TextView = binding.tvID
        val image : ImageView = binding.imgPicture
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderAccounts {
        binding = ItemAccountChatBinding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolderAccounts(binding.root)
    }

    override fun getItemCount(): Int {
        return accountArrayList.size
    }

    override fun onBindViewHolder(holder: ViewHolderAccounts, position: Int) {
        val model = accountArrayList[position]
        val fullname = model.fullName
        val image = model.image
        val uid = model.uid
        val accessStatus = model.accessStatus

        if (accessStatus == "Restricted"){
            binding.restrict.visibility = View.VISIBLE
        }
        else if (model.userType == "admin") {
            binding.admin.visibility = View.VISIBLE
        }

        holder.apply {
            Glide.with(this@MessengerUserAdapter.context)
                .load(image)
                .into(binding.imgPicture)

            binding.tvID.text = fullname

        }
        holder.itemView.setOnClickListener {
            viewDetails(model)
        }
    }

    private fun viewDetails(model: AccountsModel) {
        val bundle = Bundle()
        bundle.putString("userID", model.uid)
        bundle.putString("fulName", model.fullName)
        bundle.putString("token", model.token)
        navController.navigate(R.id.chatUserFragment, bundle)
    }

}