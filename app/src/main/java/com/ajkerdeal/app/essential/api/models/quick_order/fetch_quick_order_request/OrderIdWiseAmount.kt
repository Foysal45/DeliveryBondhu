package com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderIdWiseAmount(
    @SerializedName("orderRequestId")
    var orderRequestId: Int = 0,
    @SerializedName("requestOrderAmount")
    var requestOrderAmount: Int = 0
): Parcelable