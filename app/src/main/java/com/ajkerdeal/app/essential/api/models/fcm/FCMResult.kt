package com.ajkerdeal.app.essential.api.models.fcm

import com.google.gson.annotations.SerializedName


data class FCMResult(
    @SerializedName("message_id")
    var message_id: String = ""
)