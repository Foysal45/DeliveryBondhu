package com.ajkerdeal.app.essential.api.models.quick_order.delivery_charge


import com.google.gson.annotations.SerializedName

data class DeliveryChargeRequest(
    @SerializedName("districtId")
    var districtId: Int,
    @SerializedName("thanaId")
    var thanaId: Int,
    @SerializedName("areaId")
    var areaId: Int = 0,
    @SerializedName("serviceType")
    var serviceType: String = "" // alltoall citytocity
)