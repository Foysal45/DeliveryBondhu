package com.ajkerdeal.app.essential.api.models.status


import com.google.gson.annotations.SerializedName

data class StatusUpdateResponse(
    @SerializedName("IsSuccess")
    var isSuccess: Boolean = false,
    @SerializedName("Message")
    var message: String? = ""
)