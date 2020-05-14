package com.ajkerdeal.app.essential.api.models.auth.reset_password


import com.google.gson.annotations.SerializedName

data class UpdatePasswordRequest(
    @SerializedName("Password")
    var password: String? = "",
    @SerializedName("CustomerId")
    var customerId: Int? = 0
)