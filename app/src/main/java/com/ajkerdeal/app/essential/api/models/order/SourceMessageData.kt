package com.ajkerdeal.app.essential.api.models.order


import com.google.gson.annotations.SerializedName

data class SourceMessageData(
    @SerializedName("message")
    var message: String? = "",
    @SerializedName("instructions")
    var instructions: String? = "",
    @SerializedName("status")
    var status: String? = "",
    @SerializedName("isPay")
    var isPay: Int = 0
)