package com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request


import com.google.gson.annotations.SerializedName

data class QuickOrderListRequesst(
    @SerializedName("deliveryRiderId")
    var deliveryRiderId: Int = 0
)