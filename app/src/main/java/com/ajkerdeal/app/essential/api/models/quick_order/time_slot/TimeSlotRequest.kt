package com.ajkerdeal.app.essential.api.models.quick_order.time_slot


import com.google.gson.annotations.SerializedName

data class TimeSlotRequest(
    @SerializedName("requestDate")
    var requestDate: String? = ""
)