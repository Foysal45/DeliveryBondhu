package com.ajkerdeal.app.essential.api.models.profile


import com.google.gson.annotations.SerializedName

data class AreaInfo(
    @SerializedName("DistrictId")
    var districtId: Int = 0,
    @SerializedName("ThanaId")
    var thanaId: Int = 0,
    @SerializedName("PostCode")
    var postCode: Int = 0,
    @SerializedName("AreaId")
    var areaId: Int = 0,
    @SerializedName("DistrictName")
    var districtName: String? = "",
    @SerializedName("ThanaName")
    var thanaName: String? = ""
)