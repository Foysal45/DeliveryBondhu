package com.ajkerdeal.app.essential.api.models.pod


import com.google.gson.annotations.SerializedName

data class PodOrderRequest(
    @SerializedName("DeliveryUserId")
    var deliveryUserId: String = "",
    @SerializedName("Index")
    var index: Int = 0,
    @SerializedName("Count")
    var count: Int = 20,
    @SerializedName("StatusId")
    var statusId: String = "-1",
    @SerializedName("DtStatusId")
    var dtStatusId: String = "-1",
    @SerializedName("PodNumber")
    var podNumber: String = "-1",
    @SerializedName("Flag")
    var flag: Int = 0
)