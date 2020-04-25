package com.ajkerdeal.app.essential.api.models.order

import com.google.gson.annotations.SerializedName

data class OrderModel(
    @SerializedName("CouponId")
    var couponId: Int = 0,
    @SerializedName("ProductTitle")
    var productTitle: String? = "",
    @SerializedName("ProductPrice")
    var productPrice: Int = 0,
    @SerializedName("ProductQtn")
    var productQtn: Int = 0,
    @SerializedName("ImageUrl")
    var imageUrl: String? = "",
    @SerializedName("StatusId")
    var statusId: Int = 0



)