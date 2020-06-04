package com.ajkerdeal.app.essential.api.models.order


import com.google.gson.annotations.SerializedName

data class Action(
    @SerializedName("ActionType")
    var actionType: Int = 0,
    @SerializedName("ActionMessage")
    var actionMessage: String? = "",
    @SerializedName("UpdateStatus")
    var updateStatus: Int = 0,
    @SerializedName("StatusMessage")
    var statusMessage: String? = "",
    @SerializedName("ColorCode")
    var colorCode: String = "#FFFFFF",
    @SerializedName("CollectionPointAvailable")
    var collectionPointAvailable: Int = 0,
    @SerializedName("Icon")
    var icon: String? = "",
    @SerializedName("IsPaymentType")
    var isPaymentType: Int = 0
)