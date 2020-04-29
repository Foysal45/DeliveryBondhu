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

    @SerializedName("IsDone")
    var isDone: Int = 0,
    @SerializedName("Comments")
    var comments: String? = "",
    @SerializedName("OrderDate")
    var orderDate: String? = "",
    @SerializedName("MerchantId")
    var merchantId: Int = 0,
    @SerializedName("DealId")
    var dealId: Int = 0,
    @SerializedName("DeliveryDate")
    var deliveryDate: String? = "",
    @SerializedName("CommentedBy")
    var commentedBy: Int = 0,
    @SerializedName("PODNumber")
    var pODNumber: String? = "",
    @SerializedName("Actions")
    var actions: List<Action>? = listOf(),

    @SerializedName("SourceInfo")
    var collectionSource: CollectionSource? = null
)