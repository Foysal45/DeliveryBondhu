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
    var statusId: String = "-1"
)