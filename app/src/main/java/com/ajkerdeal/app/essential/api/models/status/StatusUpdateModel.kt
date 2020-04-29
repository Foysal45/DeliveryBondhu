package com.ajkerdeal.app.essential.api.models.status


import com.google.gson.annotations.SerializedName

data class StatusUpdateModel(
    @SerializedName("CouponId")
    var couponId: String = "",
    @SerializedName("IsDone")
    var isDone: Int = 0,
    @SerializedName("Comments")
    var comments: String = "",
    @SerializedName("OrderDate")
    var orderDate: String = "",
    @SerializedName("MerchantId")
    var merchantId: Int = 0,
    @SerializedName("DealId")
    var dealId: Int = 0,
    @SerializedName("CustomerId")
    var customerId: Int = 0,
    @SerializedName("DeliveryDate")
    var deliveryDate: String = "",
    @SerializedName("CommentedBy")
    var commentedBy: Int = 0,
    @SerializedName("PODNumber")
    var pODNumber: String = "",
    @SerializedName("HubName")
    var hubName: String = ""
)