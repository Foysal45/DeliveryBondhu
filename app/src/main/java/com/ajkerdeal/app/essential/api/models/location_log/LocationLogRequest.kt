package com.ajkerdeal.app.essential.api.models.location_log


import com.google.gson.annotations.SerializedName

data class LocationLogRequest(
    @SerializedName("riderId")
    var riderId: Int = 0,
    @SerializedName("riderName")
    var riderName: String? = "",
    @SerializedName("longitude")
    var longitude: String? = "",
    @SerializedName("latitude")
    var latitude: String? = "",
    @SerializedName("date")
    var date: String? = ""
)