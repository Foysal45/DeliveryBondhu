package com.ajkerdeal.app.essential.api.models.auth.fcm


import com.google.gson.annotations.SerializedName

data class UpdateTokenRequest(
    @SerializedName("Id")
    var id: Int? = 0,
    @SerializedName("FirebaseToken")
    var firebaseToken: String? = ""
)