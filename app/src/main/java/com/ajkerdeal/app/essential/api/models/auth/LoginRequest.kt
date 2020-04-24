package com.ajkerdeal.app.essential.api.models.auth


import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("MobileNumber")
    var mobileNumber: String? = "",
    @SerializedName("Password")
    var password: String? = ""
)