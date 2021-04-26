package com.ajkerdeal.app.essential.api.models.order


import com.google.gson.annotations.SerializedName

data class Action(
    @SerializedName("actionType")
    var actionType: Int = 0,
    @SerializedName("actionMessage")
    var actionMessage: String? = "",
    @SerializedName("updateStatus")
    var updateStatus: Int = 0,
    @SerializedName("statusMessage")
    var statusMessage: String? = "",
    @SerializedName("colorCode")
    var colorCode: String = "#FFFFFF",
    @SerializedName("collectionPointAvailable")
    var collectionPointAvailable: Int = 0,
    @SerializedName("icon")
    var icon: String? = "",
    @SerializedName("isPaymentType")
    var isPaymentType: Int = 0,
    @SerializedName("popUpDialog")
    var popUpDialogType: Int = 0
)