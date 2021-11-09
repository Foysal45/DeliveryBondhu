package com.ajkerdeal.app.essential.api.models.auth.reset_password


import com.google.gson.annotations.SerializedName

data class UpdatePasswordRequestDT(
    @SerializedName("password")
    var password: String? = "",
    @SerializedName("customerId")
    var customerId: Int? = 0
)