package com.ajkerdeal.app.essential.api.models.user_status


import com.google.gson.annotations.SerializedName

data class LocationUpdateRequest(
    @SerializedName("BondhuId")
    var bondhuId: Int = 0,
    @SerializedName("Latitude")
    var latitude: String = "",
    @SerializedName("Longitude")
    var longitude: String = ""
)