package com.ajkerdeal.app.essential.api.models.auth.otp


import com.google.gson.annotations.SerializedName

data class OTPSendRequest(
    @SerializedName("MobileOrEmail")
    var mobileOrEmail: String? = "",
    @SerializedName("CustomerId")
    var customerId: String? = "",
    @SerializedName("Type")
    var type: Int = 2
)