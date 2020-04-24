package com.ajkerdeal.app.essential.api.models.auth


import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("DeliveryUserId")
    var deliveryUserId: Int = 0,
    @SerializedName("MobileNumber")
    var mobileNumber: String? = "",
    @SerializedName("DeliveryUserName")
    var deliveryUserName: String? = "",
    @SerializedName("Message")
    var message: String? = ""
)