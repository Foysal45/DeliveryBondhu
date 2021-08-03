package com.ajkerdeal.app.essential.api.models.order


import com.google.gson.annotations.SerializedName

data class OrderRequest(
    @SerializedName("DeliveryUserId")
    var deliveryUserId: String = "0",
    @SerializedName("Index")
    var index: Int = 0,
    @SerializedName("Count")
    var count: Int = 20,
    @SerializedName("ProductTitle")
    var productTitle: String = "-1",
    @SerializedName("Flag")
    var flag: Int = 0,
    @SerializedName("StatusId")
    var statusId: String = "-1",
    @SerializedName("DtStatusId")
    var dtStatusId: String = "-1",
    @SerializedName("type")
    var serviceType: String = "",
    @SerializedName("CustomType")
    var customType: String = "no",
    @SerializedName("RiderType")
    var riderType: String = "",
    @SerializedName("OrderId")
    var orderId: String = "-1",
    @SerializedName("CollectionSlotId")
    var collectionSlotId: Int = 0
)