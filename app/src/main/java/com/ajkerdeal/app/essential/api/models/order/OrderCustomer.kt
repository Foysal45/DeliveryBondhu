package com.ajkerdeal.app.essential.api.models.order


import com.google.gson.annotations.SerializedName

data class OrderCustomer(
    @SerializedName("Id")
    var id: String = "0",
    @SerializedName("Name")
    var name: String? = "",
    @SerializedName("District")
    var district: String? = "",
    @SerializedName("MobileNumber")
    var mobileNumber: String? = "",
    @SerializedName("Address")
    var address: String? = "",
    @SerializedName("DeliveryCommission")
    var deliveryCommission: Int = 0,
    @SerializedName("TotalOrder")
    var totalOrder: Int = 0,
    @SerializedName("TotalPayment")
    var totalPayment: Int = 0,

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