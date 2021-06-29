package com.ajkerdeal.app.essential.api.models.profile.profile_DT


import com.google.gson.annotations.SerializedName

data class ProfileDataDT(
    @SerializedName("bondhuId")
    var bondhuId: Int = 0,
    @SerializedName("name")
    var name: String? = "",
    @SerializedName("mobile")
    var mobile: String? = "",
    @SerializedName("alternativeMobile")
    var alternativeMobile: String? = "",
    @SerializedName("bkashMobileNumber")
    var bkashMobileNumber: String? = "",
    @SerializedName("address")
    var address: String? = "",
    @SerializedName("areaInfo")
    var areaInfo: List<AreaInfo>? = listOf()
)