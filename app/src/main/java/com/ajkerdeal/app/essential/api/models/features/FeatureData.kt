package com.ajkerdeal.app.essential.api.models.features


import com.google.gson.annotations.SerializedName

data class FeatureData(
    @SerializedName("BannerUrl")
    var bannerUrl: String? = "",
    @SerializedName("WebUrl")
    var webUrl: String? = ""
)