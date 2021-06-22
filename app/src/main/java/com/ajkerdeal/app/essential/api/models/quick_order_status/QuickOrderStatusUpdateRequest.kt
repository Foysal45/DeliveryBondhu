package com.ajkerdeal.app.essential.api.models.quick_order_status


import com.google.gson.annotations.SerializedName

data class QuickOrderStatusUpdateRequest(
    @SerializedName("orderRequestId")
    var orderRequestId: Int = 0,
    @SerializedName("deliveryUserId")
    var deliveryUserId: Int = 0,
    @SerializedName("status")
    var status: Int = 0
)