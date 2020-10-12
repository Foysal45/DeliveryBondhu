package com.ajkerdeal.app.essential.api.models.update_doc


import com.google.gson.annotations.SerializedName

data class UpdateDocRequest(
    @SerializedName("orderId")
    var orderId: String?,
    @SerializedName("documentUrl")
    var documentUrl: String?
)