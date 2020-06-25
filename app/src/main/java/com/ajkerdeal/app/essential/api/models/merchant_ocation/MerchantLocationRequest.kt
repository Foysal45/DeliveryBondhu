package com.ajkerdeal.app.essential.api.models.merchant_ocation


import com.google.gson.annotations.SerializedName

data class MerchantLocationRequest(
    @SerializedName("CourierUserId")
    var courierUserId: Int? = 0,
    @SerializedName("DistrictId")
    var districtId: Int? = 0,
    @SerializedName("ThanaId")
    var thanaId: Int? = 0,
    @SerializedName("Latitude")
    var latitude: String? = "0.0",
    @SerializedName("Longitude")
    var longitude: String? = "0.0"
)