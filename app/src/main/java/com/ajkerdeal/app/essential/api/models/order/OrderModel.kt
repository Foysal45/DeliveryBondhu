package com.ajkerdeal.app.essential.api.models.order

import com.google.gson.annotations.SerializedName

data class OrderModel(
    @SerializedName("CouponId")
    var couponId: String = "",
    @SerializedName("ProductTitle")
    var productTitle: String? = "",
    @SerializedName("ProductPrice")
    var productPrice: Int = 0,
    @SerializedName("ProductQtn")
    var productQtn: Int = 0,
    @SerializedName("ImageUrl")
    var imageUrl: String? = "",
    @SerializedName("StatusId")
    var statusId: Int = 0,
    @SerializedName("BondhuCharge")
    var bondhuCharge: Int = 0,
    @SerializedName("DeliveryCharge")
    var deliveryCharge: Int = 0,
    @SerializedName("Colors")
    var colors: String? = "",
    @SerializedName("Sizes")
    var sizes: String? = "",
    @SerializedName("DeliveryType")
    var deliveryType: String? = "",

    @SerializedName("Comments")
    var comments: String? = "",
    @SerializedName("OrderDate")
    var orderDate: String? = "",
    @SerializedName("MerchantId")
    var merchantId: Int = 0,
    @SerializedName("DealId")
    var dealId: String = "0",
    @SerializedName("DeliveryDate")
    var deliveryDate: String? = "",
    @SerializedName("CommentedBy")
    var commentedBy: Int = 0,
    @SerializedName("PODNumber")
    var pODNumber: String? = "",
    @SerializedName("CollectionPointId")
    var collectionPointId: Int = 0,
    @SerializedName("CustomerId")
    var customerId: String? = "0",
    @SerializedName("Actions")
    var actions: List<Action>? = listOf(),

    @SerializedName("CollectionTimeSlot")
    var collectionTimeSlot: CollectionTimeSlot? = CollectionTimeSlot(),

    @SerializedName("SourceInfo")
    var collectionSource: CollectionSource? = null
)