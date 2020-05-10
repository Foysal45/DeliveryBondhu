package com.ajkerdeal.app.essential.api.models.auth.signup


import com.google.gson.annotations.SerializedName

data class SignUpResponse(
    @SerializedName("Message")
    var message: String? = "",
    @SerializedName("CustomerId")
    var customerId: Int? = 0
)