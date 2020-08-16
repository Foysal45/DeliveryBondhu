package com.ajkerdeal.app.essential.api.models.location_log


import com.google.gson.annotations.SerializedName

data class LocationLogResponse(
    @SerializedName("message")
    var message: String? = "",
    @SerializedName("status")
    var status: Boolean = false
)