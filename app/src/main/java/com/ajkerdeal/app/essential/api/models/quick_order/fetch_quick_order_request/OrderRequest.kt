package com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request


import com.google.gson.annotations.SerializedName

data class OrderRequest(
    @SerializedName("orderRequestId")
    var orderRequestId: Int = 0,
    @SerializedName("courierUserId")
    var courierUserId: Int = 0,
    @SerializedName("requestOrderAmount")
    var requestOrderAmount: Int = 0,
    @SerializedName("requestDate")
    var requestDate: String? = "",
    @SerializedName("districtId")
    var districtId: Int = 0,
    @SerializedName("thanaId")
    var thanaId: Int = 0,
    @SerializedName("collectionDate")
    var collectionDate: String? = "",
    @SerializedName("collectionTimeSlotId")
    var collectionTimeSlotId: Int = 0,
    @SerializedName("deliveryUserId")
    var deliveryUserId: Int = 0,
    @SerializedName("status")
    var status: Int = 0,
    @SerializedName("totalOrder")
    var totalOrder: Int = 0,
    @SerializedName("collectionTimeSlot")
    var collectionTimeSlot: CollectionTimeSlot = CollectionTimeSlot(),
    @SerializedName("actionModel")
    var actionModel: List<ActionModel> = listOf(),
    @SerializedName("courierUsersView")
    var courierUsersView: String? = "",
    @SerializedName("orderRequestSelfList")
    var orderRequestSelfList: List<OrderIdWiseAmount> = listOf()
)