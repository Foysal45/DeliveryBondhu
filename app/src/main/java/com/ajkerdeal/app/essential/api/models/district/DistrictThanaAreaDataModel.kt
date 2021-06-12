package com.ajkerdeal.app.essential.api.models.district


import com.google.gson.annotations.SerializedName

data class DistrictThanaAreaDataModel(
    @SerializedName("districtId")
    var districtId: Int = 0,
    @SerializedName("district")
    var district: String? = "",
    @SerializedName("districtBng")
    var districtBng: String? = "",
    @SerializedName("areaType")
    var areaType: Int = 0,
    @SerializedName("parentId")
    var parentId: Int = 0,
    @SerializedName("postalCode")
    var postalCode: String? = "",
    @SerializedName("isCity")
    var isCity: Boolean = false,
    @SerializedName("isActive")
    var isActive: Boolean = false,
    @SerializedName("isActiveForCorona")
    var isActiveForCorona: Boolean = false,
    @SerializedName("isPickupLocation")
    var isPickupLocation: Boolean = false,
    @SerializedName("districtPriority")
    var districtPriority: Int = 0,
    @SerializedName("redxHubName")
    var redxHubName: String? = "",
    @SerializedName("updatedBy")
    var updatedBy: Int = 0,
    @SerializedName("updatedOn")
    var updatedOn: String? = "",
    @SerializedName("redxAreaId")
    var redxAreaId: Int = 0,
    @SerializedName("redxAreaName")
    var redxAreaName: String? = "",
    @SerializedName("paperflyAreaName")
    var paperflyAreaName: String? = "",
    @SerializedName("isDtOwnSecondMileDelivery")
    var isDtOwnSecondMileDelivery: Boolean = false,
    @SerializedName("eDeshMobileNo")
    var eDeshMobileNo: String? = "",
    @SerializedName("hasExpressDelivery")
    var hasExpressDelivery: Int = 0,
    @SerializedName("tigerMobileNo")
    var tigerMobileNo: String? = "",
    @SerializedName("ownSecondMileDelivery")
    var ownSecondMileDelivery: String? = ""
)