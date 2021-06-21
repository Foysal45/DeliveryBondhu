package com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request


import com.google.gson.annotations.SerializedName

data class ActionModel(
    @SerializedName("buttonName")
    var buttonName: String? = "",
    @SerializedName("statusUpdate")
    var statusUpdate: Int = 0,
    @SerializedName("statusMessage")
    var statusMessage: String? = "",
    @SerializedName("colorCode")
    var colorCode: String? = ""
)