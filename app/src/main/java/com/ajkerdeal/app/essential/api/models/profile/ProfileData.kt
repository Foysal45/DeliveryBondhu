package com.ajkerdeal.app.essential.api.models.profile


import com.google.gson.annotations.SerializedName

data class ProfileData(
    @SerializedName("Id")
    var id: Int? = 0,
    @SerializedName("Name")
    var name: String? = "",
    @SerializedName("Mobile")
    var mobile: String? = "",
    @SerializedName("AlternativeMobile")
    var alternativeMobile: String? = "",
    @SerializedName("Address")
    var address: String? = "",

    @SerializedName("imageInfo")
    var imageInfo: ImageInfo? = null,
    @SerializedName("AreaInfo")
    var areaInfo: List<AreaInfo>? = listOf()
)