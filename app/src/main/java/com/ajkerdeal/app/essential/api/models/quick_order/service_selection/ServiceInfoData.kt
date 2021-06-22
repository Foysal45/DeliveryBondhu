package com.ajkerdeal.app.essential.api.models.quick_order.service_selection

import com.google.gson.annotations.SerializedName

data class ServiceInfoData(
    @SerializedName("serviceTypeName")
    var serviceTypeName: String? = "",
    @SerializedName("serviceInfo")
    var serviceInfo: String? = "",
    @SerializedName("deliveryRangeId")
    var deliveryRangeId: List<Int> = listOf()

)