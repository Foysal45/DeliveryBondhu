package com.ajkerdeal.app.essential.api.models.auth.reset_password


import com.google.gson.annotations.SerializedName

data class CheckMobileRequest(
    @SerializedName("MobileNumber")
    var mobileNumber: String? = ""
)