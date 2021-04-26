package com.ajkerdeal.app.essential.api.models.order


import com.google.gson.annotations.SerializedName

data class OrderCustomer(
    @SerializedName("id")
    var id: String = "0",
    @SerializedName("name")
    var name: String? = "",
    @SerializedName("district")
    var district: String? = "",
    @SerializedName("mobileNumber")
    var mobileNumber: String? = "",
    @SerializedName("address")
    var address: String? = "",
    @SerializedName("deliveryCommission")
    var deliveryCommission: Int = 0,
    @SerializedName("totalOrder")
    var totalOrder: Int = 0,
    @SerializedName("totalPayment")
    var totalPayment: Int = 0,
    @SerializedName("bondhuCharge")
    var bondhuCharge: Int = 0,

    @SerializedName("latitude")
    var latitude: String? = "",
    @SerializedName("longitude")
    var longitude: String? = "",
    @SerializedName("collectAddressDistrictId")
    var collectAddressDistrictId: Int = 0,
    @SerializedName("collectAddressThanaId")
    var collectAddressThanaId: Int = 0,
    @SerializedName("merchantId")
    var merchantId: Int = 0,

    @SerializedName("actions")
    var actions: List<Action>? = listOf(),
    @SerializedName("sourceInfo")
    var collectionSource: CollectionSource? = null,
    @SerializedName("customerOrderDataModel")
    var orderList: List<OrderModel>? = listOf(),
    @SerializedName("customerMessageData")
    var customerMessageData: SourceMessageData? = null,

    // internal
    var state: Boolean = false,
    var isLocationUpdated: Boolean = false
)