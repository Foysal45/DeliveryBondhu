package com.ajkerdeal.app.essential.api.models.quick_order.delivery_charge


import com.google.gson.annotations.SerializedName

data class DeliveryTypeModel(
    @SerializedName("deliveryType")
    var deliveryType: String,
    @SerializedName("deliveryDayChargeModel")
    var deliveryDayChargeModel: List<DeliveryDayChargeModel>
)