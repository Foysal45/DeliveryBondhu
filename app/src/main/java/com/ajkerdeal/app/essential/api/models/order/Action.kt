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
    var colorCode: String = "#FFFFFF"
)