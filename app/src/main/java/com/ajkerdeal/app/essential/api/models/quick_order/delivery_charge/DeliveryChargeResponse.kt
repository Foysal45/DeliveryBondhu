package com.ajkerdeal.app.essential.api.models.quick_order.delivery_charge


import com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request.WeightRangeWiseData
import com.google.gson.annotations.SerializedName

data class DeliveryChargeResponse(
    @SerializedName("weightRangeId")
    var weightRangeId: Int = 0,
    @SerializedName("weight")
    var weight: String = "",
    @SerializedName("isOpenBox")
    var isOpenBox: Boolean = false,
    @SerializedName("weightRangeWiseData")
    var weightRangeWiseData: List<WeightRangeWiseData> = listOf(),
    @SerializedName("deliveryTypeModel")
    var deliveryTypeModel: List<DeliveryTypeModel> = listOf()
)