package com.ajkerdeal.app.essential.api.models.update_doc


import com.google.gson.annotations.SerializedName

data class UpdateDocRequestDT(
    @SerializedName("courierOrdersId")
    var courierOrdersId: String?,
    @SerializedName("documentUrl")
    var documentUrl: String?
)