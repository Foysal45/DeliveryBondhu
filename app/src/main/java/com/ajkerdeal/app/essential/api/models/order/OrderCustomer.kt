package com.ajkerdeal.app.essential.api.models.order


import com.google.gson.annotations.SerializedName

data class OrderCustomer(
    @SerializedName("CustomerId")
    var customerId: String = "0",
    @SerializedName("CustomerName")
    var customerName: String? = "",
    @SerializedName("CustomerMobileNumber")
    var customerMobileNumber: String? = "",
    @SerializedName("CustomerAddress")
    var customerAddress: String? = "",
    @SerializedName("DeliveryCommission")
    var deliveryCommission: Int = 0,
    @SerializedName("TotalOrder")
    var totalOrder: Int = 0,

    @SerializedName("Actions")
    var actions: List<Action>? = listOf(),
    @SerializedName("SourceInfo")
    var collectionSource: CollectionSource? = null,
    @SerializedName("CustomerOrderDataModel")
    var orderList: List<OrderModel>? = listOf(),
    @SerializedName("CustomerMessageData")
    var customerMessageData: SourceMessageData? = null,

    // internal
    var state: Boolean = false
)