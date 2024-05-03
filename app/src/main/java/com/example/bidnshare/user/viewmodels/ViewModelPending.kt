package com.example.bidnshare.user.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bidnshare.models.PendingItem

class ViewModelPending : ViewModel() {
    private val _pendingItems: MutableLiveData<List<PendingItem>> = MutableLiveData()
    val pendingItems: LiveData<List<PendingItem>> = _pendingItems

    fun addPendingItem(item: PendingItem) {
        val currentList = _pendingItems.value ?: listOf()
        _pendingItems.value = currentList + item
    }

    fun setPendingItems(items: List<PendingItem>) {
        _pendingItems.value = items
    }
}