package com.ajkerdeal.app.essential.fcm


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
data class FCMData(
    @SerializedName("notificationType")
    var notificationType: String? = "",
    @SerializedName("title")
    var title: String? = "",
    @SerializedName("description")
    var description: String? = "",
    @SerializedName("imageLink")
    var imageLink: String? = "",
    @SerializedName("productImage")
    var productImage: String? = "",
    @SerializedName("bigText")
    var bigText: String? = "",
    @SerializedName("serviceType")
    var serviceType: String? = ""
): Parcelable