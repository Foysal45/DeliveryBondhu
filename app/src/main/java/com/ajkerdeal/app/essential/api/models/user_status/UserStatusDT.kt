package com.ajkerdeal.app.essential.api.models.user_status


import com.google.gson.annotations.SerializedName

data class UserStatusDT(
    @SerializedName("id")
    var id: Int = 0,
    @SerializedName("name")
    var name: String? = "",
    @SerializedName("mobile")
    var mobile: String? = "",
    @SerializedName("isActive")
    var isActive: Int = 0,
    @SerializedName("isNowOffline")
    var isNowOffline: Boolean = false,
    @SerializedName("latitude")
    var latitude: String? = "",
    @SerializedName("longitude")
    var longitude: String? = "",
    @SerializedName("updatedOn")
    var updatedOn: String? = "",
    @SerializedName("updatedBy")
    var updatedBy: Int = 0,
    @SerializedName("isPermanentRider")
    var isPermanentRider: Boolean = false,
    @SerializedName("firebaseToken")
    var firebaseToken: String? = "",
    @SerializedName("riderType")
    var riderType: String? = "",
    @SerializedName("hubName")
    var hubName: String? = ""
)