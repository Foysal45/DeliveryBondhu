package com.ajkerdeal.app.essential.api.models.user_status


import com.google.gson.annotations.SerializedName

data class UserStatus(
    @SerializedName("UserType")
    var userType: Int = 0,
    @SerializedName("RiderType")
    var riderType: String = "",
    @SerializedName("IsNowOffline")
    var isNowOffline: Boolean = false,
    @SerializedName("LocationUpdateIntervalInMinute")
    var locationUpdateIntervalInMinute: Int = 20,
    @SerializedName("LocationDistanceInMeter")
    var locationDistanceInMeter: Int = 20,

    @SerializedName("IsProfileImage")
    var isProfileImage: Boolean = false,
    @SerializedName("IsDrivingLicense")
    var isDrivingLicense: Boolean = false,
    @SerializedName("IsNID")
    var isNID: Boolean = false,
    @SerializedName("ProfileImage")
    var profileImage: String? = ""
)