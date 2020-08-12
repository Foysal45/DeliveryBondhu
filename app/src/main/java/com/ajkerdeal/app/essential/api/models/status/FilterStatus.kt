package com.ajkerdeal.app.essential.api.models.status


import com.google.gson.annotations.SerializedName

data class FilterStatus(
    @SerializedName("StatusName")
    var statusName: String? = "",
    @SerializedName("Status")
    var status: String = "-1",
    @SerializedName("DtStatus")
    var dtStatus: String = "-1",
    @SerializedName("Flag")
    var flag: Int = 0,
    @SerializedName("CollectionFilter")
    var collectionFilter: Int = 0,
    @SerializedName("CustomType")
    var customType: String = "no",
    @SerializedName("isUnavailableShow")
    var isUnavailableShow: Boolean = false,
    @SerializedName("allowLocationAdd")
    var allowLocationAdd: Boolean = false,
    @SerializedName("allowPrint")
    var allowPrint: Boolean = false
)