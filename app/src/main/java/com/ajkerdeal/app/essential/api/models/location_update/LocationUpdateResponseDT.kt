package com.ajkerdeal.app.essential.api.models.location_update

import com.google.gson.annotations.SerializedName

data class LocationUpdateResponseDT(
        @SerializedName("id")
        var id: Int? = 0,
        @SerializedName("districtId")
        var districtId: Int? = 0,
        @SerializedName("thanaId")
        var thanaId: Int? = 0,
        @SerializedName("areaId")
        var areaId: Int? = 0,
        @SerializedName("courierUserId")
        var courierUserId: Int? = 0,
        @SerializedName("pickupAddress")
        var pickupAddress: String? = "0.0",
        @SerializedName("latitude")
        var latitude: String? = "0.0",
        @SerializedName("longitude")
        var longitude: String? = "0.0"
)
