package com.ajkerdeal.app.essential.api.models.profile


import com.google.gson.annotations.SerializedName

data class ImageInfo(
    @SerializedName("Nid")
    var nid: String? = "",
    @SerializedName("ProfileImage")
    var profileImage: String? = "",
    @SerializedName("DrivingImage")
    var drivingImage: String? = ""
)