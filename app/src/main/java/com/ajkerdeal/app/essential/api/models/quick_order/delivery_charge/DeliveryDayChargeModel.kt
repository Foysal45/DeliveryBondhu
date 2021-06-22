package com.ajkerdeal.app.essential.api.models.quick_order.delivery_charge


import com.google.gson.annotations.SerializedName

data class DeliveryDayChargeModel(
    @SerializedName("weightRangeId")
    var weightRangeId: Int = 0,
    @SerializedName("deliveryType")
    var deliveryType: String = "",
    @SerializedName("chargeAmount")
    var chargeAmount: Double = 0.0,
    @SerializedName("days")
    var days: String = "",
    @SerializedName("dayType")
    var dayType: String = ""
)