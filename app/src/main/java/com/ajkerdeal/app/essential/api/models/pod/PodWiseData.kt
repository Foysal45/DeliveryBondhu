package com.ajkerdeal.app.essential.api.models.pod


import com.ajkerdeal.app.essential.api.models.order.Action
import com.ajkerdeal.app.essential.api.models.order.OrderCustomer
import com.ajkerdeal.app.essential.api.models.order.SourceMessageData
import com.google.gson.annotations.SerializedName

data class PodWiseData(
    @SerializedName("PodNumber")
    var podNumber: String? = "",
    @SerializedName("CollectionAddress")
    var collectionAddress: String? = "",
    @SerializedName("TotalPodCommission")
    var totalPodCommission: Int = 0,
    @SerializedName("TotalCustomer")
    var totalCustomer: Int = 0,

    @SerializedName("Actions")
    var actions: List<Action>? = listOf(),
    @SerializedName("CustomerDataModel")
    var customerDataModel: List<OrderCustomer>? = listOf(),
    @SerializedName("CustomerMessageData")
    var customerMessageData: SourceMessageData? = null,

    // internal
    var state: Boolean = false
)