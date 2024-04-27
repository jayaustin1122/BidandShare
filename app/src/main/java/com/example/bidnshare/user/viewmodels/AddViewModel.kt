package com.example.bidnshare.user.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddViewModel: ViewModel() {
    private val _imageList = MutableLiveData<List<Uri>>()
    val imageList: LiveData<List<Uri>> get() = _imageList

    fun addImages(uris: List<Uri>) {
        val list = _imageList.value?.toMutableList() ?: mutableListOf()
        list.addAll(uris)
        _imageList.value = list
    }

    fun clearImageList() {
        _imageList.value = emptyList()
    }
}