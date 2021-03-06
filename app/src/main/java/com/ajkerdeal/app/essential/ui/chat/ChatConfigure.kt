package com.ajkerdeal.app.essential.ui.chat

import android.content.Context
import android.content.Intent
import androidx.core.os.bundleOf
import com.ajkerdeal.app.essential.api.models.chat.ChatUserData
import com.ajkerdeal.app.essential.api.models.chat.FirebaseCredential

class ChatConfigure constructor(
    private val documentName: String,
    private val sender: ChatUserData,
    private val receiver: ChatUserData = ChatUserData(),
    private val firebaseCredential: FirebaseCredential? = null,
) {

    fun config(context: Context) {
        Intent(context, ChatActivity::class.java).apply {
            val bundle = bundleOf(
                "credential" to firebaseCredential,
                "documentName" to documentName,
                "sender" to sender,
                "receiver" to receiver
            )
            putExtra("chatConfig", bundle)
        }.also {
            context.startActivity(it)
        }
    }
    
}