package com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request


import com.google.gson.annotations.SerializedName

data class CollectionTimeSlot(
    @SerializedName("collectionTimeSlotId")
    var collectionTimeSlotId: Int = 0,
    @SerializedName("startTime")
    var startTime: String? = "",
    @SerializedName("endTime")
    var endTime: String? = "",
    @SerializedName("ordering")
    var ordering: Int = 0,
    @SerializedName("isActive")
    var isActive: Boolean = false,
    @SerializedName("orderLimit")
    var orderLimit: Int = 0,
    @SerializedName("cutOffTime")
    var cutOffTime: String? = "",
)