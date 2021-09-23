package com.ajkerdeal.app.essential.api.models.order

import com.google.gson.annotations.SerializedName

data class OrderModel(
    @SerializedName("couponId")
    var couponId: String = "",
    @SerializedName("productTitle")
    var productTitle: String? = "",
    @SerializedName("productPrice")
    var productPrice: Int = 0,
    @SerializedName("productQtn")
    var productQtn: Int = 0,
    @SerializedName("imageUrl")
    var imageUrl: String? = "",
    @SerializedName("statusId")
    var statusId: Int = 0,
    @SerializedName("bondhuCharge")
    var bondhuCharge: Int = 0,
    @SerializedName("deliveryCharge")
    var deliveryCharge: Int = 0,
    @SerializedName("colors")
    var colors: String? = "",
    @SerializedName("sizes")
    var sizes: String? = "",
    @SerializedName("deliveryType")
    var deliveryType: String? = "",
    @SerializedName("isAdvancePayment")
    var isAdvancePayment: Boolean = false,

    @SerializedName("comments")
    var comments: String? = "",
    @SerializedName("isDone")
    var isDone: Int? = 0,
    @SerializedName("hubName")
    var hubName: String? = "",
    @SerializedName("orderDate")
    var orderDate: String? = "",
    @SerializedName("merchantId")
    var merchantId: Int = 0,
    @SerializedName("dealId")
    var dealId: String = "0",
    @SerializedName("deliveryDate")
    var deliveryDate: String? = "",
    @SerializedName("commentedBy")
    var commentedBy: Int = 0,
    @SerializedName("podNumber")
    var pODNumber: String? = "",
    @SerializedName("collectionPointId")
    var collectionPointId: Int = 0,
    @SerializedName("customerId")
    var customerId: String? = "0",
    @SerializedName("deliveryRangeId")
    var deliveryRangeId: Int = 0,
    @SerializedName("weightRangeId")
    var weightRangeId: Int = 0,
    @SerializedName("priorityService")
    var priorityService: Int = 0,
    @SerializedName("actions")
    var actions: List<Action>? = listOf(),

    @SerializedName("collectionTimeSlot")
    var collectionTimeSlot: CollectionTimeSlot? = CollectionTimeSlot(),

    @SerializedName("sourceInfo")
    var collectionSource: CollectionSource? = null,

    @SerializedName("isHeavyWeight")
    var isHeavyWeight: Boolean = false
)