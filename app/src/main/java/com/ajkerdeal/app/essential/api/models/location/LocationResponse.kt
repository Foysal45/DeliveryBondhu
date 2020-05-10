package com.ajkerdeal.app.essential.api.models.location

import com.google.gson.annotations.SerializedName

data class LocationResponse(
    @SerializedName("DistrictInfo")
    var districtInfo: List<DistrictInfoModel> = listOf(),
    @SerializedName("Area")
    var area: List<AreaInfoModel> = listOf()
)