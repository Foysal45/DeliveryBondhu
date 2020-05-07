package com.ajkerdeal.app.essential.api.models.status


import com.google.gson.annotations.SerializedName

data class FilterStatus(
    @SerializedName("StatusName")
    var statusName: String? = "",
    @SerializedName("Status")
    var status: String = "-1",
    @SerializedName("Flag")
    var flag: Int = 0
)