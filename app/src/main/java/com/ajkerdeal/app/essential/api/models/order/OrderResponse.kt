package com.ajkerdeal.app.essential.api.models.order


import com.google.gson.annotations.SerializedName

data class OrderResponse(
    @SerializedName("totalCount")
    var totalCount: Int = 0,
    @SerializedName("customerOrderResponseModel")
    var customerOrderList: List<OrderCustomer>? = listOf()
)