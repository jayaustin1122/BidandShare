package com.example.bidnshare.user.chat

data class ChatModel(var senderId:String = "",
                     var receiverId:String = "",
                     var message:String = "",
                 )