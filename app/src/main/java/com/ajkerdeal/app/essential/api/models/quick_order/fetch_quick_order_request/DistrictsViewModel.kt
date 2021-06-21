package com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request


import com.google.gson.annotations.SerializedName

data class DistrictsViewModel(
    @SerializedName("districtId")
    var districtId: Int = 0,
    @SerializedName("district")
    var district: String? = "",
    @SerializedName("thanaId")
    var thanaId: Int = 0,
    @SerializedName("thana")
    var thana: String? = "",
    @SerializedName("area")
    var area: String? = "",
    @SerializedName("edeshMobileNo")
    var edeshMobileNo: String? = "",
    @SerializedName("tigerMobileNo")
    var tigerMobileNo: String? = "",
)