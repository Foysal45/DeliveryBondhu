package com.ajkerdeal.app.essential.api.models.collection


import com.google.gson.annotations.SerializedName

data class CollectionData(
    @SerializedName("ImageUrl")
    var imageUrl: String? = "",
    @SerializedName("ProductTitle")
    var productTitle: String? = "",
    @SerializedName("ProductPrice")
    var productPrice: Int = 0,
    @SerializedName("ProductQtn")
    var productQtn: Int = 1,
    @SerializedName("IsPay")
    var isPay: Int = 0
)