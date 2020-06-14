package com.ajkerdeal.app.essential.api.models.profile


import com.google.gson.annotations.SerializedName

data class ProfileData(
    @SerializedName("BondhuId")
    var bondhuId: Int? = 0,
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
    var areaInfo: List<AreaInfo>? = listOf(),

    @SerializedName("IsProfileImage")
    var isProfileImage: Boolean = false,
    @SerializedName("IsDrivingLicense")
    var isDrivingLicense: Boolean = false,
    @SerializedName("IsNID")
    var isNID: Boolean = false
)