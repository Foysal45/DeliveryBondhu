package com.ajkerdeal.app.essential.api.models.pod


import com.google.gson.annotations.SerializedName

data class PodOrderResponse(
    @SerializedName("TotalCount")
    var totalCount: Int = 0,
    @SerializedName("PodWiseDataModel")
    var podWiseDataModel: List<PodWiseData>? = listOf()
)