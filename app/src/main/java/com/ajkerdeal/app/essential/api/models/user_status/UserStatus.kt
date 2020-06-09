package com.ajkerdeal.app.essential.api.models.user_status


import com.google.gson.annotations.SerializedName

data class UserStatus(
    @SerializedName("UserType")
    var userType: Int = 0,
    @SerializedName("IsNowOffline")
    var isNowOffline: Boolean = false,
    @SerializedName("LocationUpdateIntervalInMinute")
    var locationUpdateIntervalInMinute: Int = 0

)