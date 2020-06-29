package com.ajkerdeal.app.essential.api.models.status_location


import com.google.gson.annotations.SerializedName

data class StatusLocationRequest(
    @SerializedName("MerchantId")
    var merchantId: Int? = 0,
    @SerializedName("DeliveryUserId")
    var deliveryUserId: Int? = 0,
    @SerializedName("Latitude")
    var latitude: String? = "",
    @SerializedName("Longitude")
    var longitude: String? = ""
)