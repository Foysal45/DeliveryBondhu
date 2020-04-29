package com.ajkerdeal.app.essential.api.models.order


import com.google.gson.annotations.SerializedName

data class CollectionSource(
    @SerializedName("SourcePersonName")
    var sourcePersonName: String? = "",
    @SerializedName("SourceAddress")
    var sourceAddress: String? = "",
    @SerializedName("SourceMobile")
    var sourceMobile: String? = "",
    @SerializedName("SourceDealPrice")
    var sourceDealPrice: Int? = 0
)