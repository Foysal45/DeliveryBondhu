package com.ajkerdeal.app.essential.api.models.profile.profile_DT


import com.google.gson.annotations.SerializedName

data class AreaInfo(
    @SerializedName("districtId")
    var districtId: Int = 0,
    @SerializedName("districtName")
    var districtName: String? = "",
    @SerializedName("thanaId")
    var thanaId: Int = 0,
    @SerializedName("thanaName")
    var thanaName: String? = "",
    @SerializedName("postCode")
    var postCode: Int = 0,
    @SerializedName("areaId")
    var areaId: Int = 0,
    @SerializedName("areaName")
    var areaName: String? = ""
)