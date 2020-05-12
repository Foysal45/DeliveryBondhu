package com.ajkerdeal.app.essential.api.models.collection


import com.google.gson.annotations.SerializedName

data class CollectionRequest(
    @SerializedName("CouponId")
    var couponId: Int? = 0,
    @SerializedName("DeliveryUserId")
    var deliveryUserId: Int? = 0,
    @SerializedName("CollectionPointId")
    var collectionPointId: Int? = 0
)