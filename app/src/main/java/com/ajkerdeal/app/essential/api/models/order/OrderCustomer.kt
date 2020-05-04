package com.ajkerdeal.app.essential.api.models.order


import com.google.gson.annotations.SerializedName

data class OrderCustomer(
    @SerializedName("CustomerId")
    var customerId: Int = 0,
    @SerializedName("CustomerName")
    var customerName: String? = "",
    @SerializedName("CustomerMobileNumber")
    var customerMobileNumber: String? = "",
    @SerializedName("CustomerAddress")
    var customerAddress: String? = "",

    @SerializedName("Actions")
    var actions: List<Action>? = listOf(),
    @SerializedName("SourceInfo")
    var collectionSource: CollectionSource? = null,
    @SerializedName("CustomerOrderDataModel")
    var orderList: List<OrderModel>? = listOf(),

    // internal
    var state: Boolean = false
)