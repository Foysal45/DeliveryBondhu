package com.ajkerdeal.app.essential.api.models.order


import com.google.gson.annotations.SerializedName

data class CollectionTimeSlot(
    @SerializedName("StartTime")
    var startTime: String? = "00:00:00",
    @SerializedName("EndTime")
    var endTime: String? = "00:00:00"
)