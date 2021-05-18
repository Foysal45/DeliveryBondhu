package com.ajkerdeal.app.essential.api.models.status
import com.google.gson.annotations.SerializedName

data class DTStatusUpdateModel(
    @SerializedName("updatedBy")
    var updatedBy: Int = 0,
    @SerializedName("courierOrdersId")
    var courierOrdersId: String? = "",
    @SerializedName("status")
    var status: Int = 0,
    @SerializedName("comment")
    var comment: String? = ""
)