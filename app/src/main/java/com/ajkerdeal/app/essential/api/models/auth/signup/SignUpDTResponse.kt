package com.ajkerdeal.app.essential.api.models.auth.signup


import com.google.gson.annotations.SerializedName

data class SignUpDTResponse(
    @SerializedName("customerId")
    var customerId: Int = 0,
    @SerializedName("message")
    var message: String? = ""
)