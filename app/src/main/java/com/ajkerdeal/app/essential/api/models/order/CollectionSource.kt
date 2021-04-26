package com.ajkerdeal.app.essential.api.models.order


import com.google.gson.annotations.SerializedName

data class CollectionSource(
    @SerializedName("sourcePersonName")
    var sourcePersonName: String? = "",
    @SerializedName("sourceAddress")
    var sourceAddress: String? = "",
    @SerializedName("sourceMobile")
    var sourceMobile: String? = "",
    @SerializedName("sourceDealPrice")
    var sourceDealPrice: Int? = 0,
    @SerializedName("sourceMessageData")
    var sourceMessageData: SourceMessageData? = null
)