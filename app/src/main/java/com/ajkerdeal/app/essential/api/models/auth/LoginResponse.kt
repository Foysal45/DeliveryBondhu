package com.ajkerdeal.app.essential.api.models.auth


import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("deliveryUserId")
    var deliveryUserId: Int = 0,
    @SerializedName("mobileNumber")
    var mobileNumber: String? = "",
    @SerializedName("bkashMobileNumber")
    var bkashMobileNumber: String? = "",
    @SerializedName("profileImage")
    var profileImage: String? = "",
    @SerializedName("deliveryUserName")
    var deliveryUserName: String? = "",
    @SerializedName("message")
    var message: String? = ""
)