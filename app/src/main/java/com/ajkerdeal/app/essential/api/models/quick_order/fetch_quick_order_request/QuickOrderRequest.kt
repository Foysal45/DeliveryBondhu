package com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request


import com.google.gson.annotations.SerializedName

data class QuickOrderRequest(
    @SerializedName("deliveryRiderId")
    var deliveryRiderId: Int = 0,
    @SerializedName("statusId")
    var statusId: Int = 0
)