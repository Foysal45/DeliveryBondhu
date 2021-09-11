package com.ajkerdeal.app.essential.api.models.chat

data class ChatData(
    var id: String = "",
    var message: String = "",
    var url: String = "",
    var type: String = "",
    var profile: String = "",
    var date: Long = 0L
)
