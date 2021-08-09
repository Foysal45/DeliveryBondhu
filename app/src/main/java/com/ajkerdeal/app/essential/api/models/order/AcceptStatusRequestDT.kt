package com.ajkerdeal.app.essential.api.models.order


import com.google.gson.annotations.SerializedName

data class AcceptStatusRequestDT(
    @SerializedName("updatedBy")
    var updatedBy: Int = 0,
    @SerializedName("courierOrdersId")
    var courierOrdersId: String? = "",
    @SerializedName("collectionTimeSlotId")
    var collectionTimeSlotId: Int = 0
)