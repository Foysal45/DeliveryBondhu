package com.ajkerdeal.app.essential.api.models.order


import com.google.gson.annotations.SerializedName

data class CollectionTimeSlot(
    @SerializedName("collectionTimeSlotId")
    var collectionTimeSlotId: Int? = 0,
    @SerializedName("startTime")
    var startTime: String? = "00:00:00",
    @SerializedName("endTime")
    var endTime: String? = "00:00:00",
    @SerializedName("ordering")
    var ordering: Int? = 0,
    @SerializedName("isActive")
    var isActive: Boolean? = false,
)