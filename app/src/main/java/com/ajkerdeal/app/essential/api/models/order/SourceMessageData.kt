package com.ajkerdeal.app.essential.api.models.order


import com.google.gson.annotations.SerializedName

data class SourceMessageData(
    @SerializedName("Message")
    var message: String? = "",
    @SerializedName("Instructions")
    var instructions: String? = "",
    @SerializedName("Status")
    var status: String? = "",
    @SerializedName("IsPay")
    var isPay: Int = 0
)