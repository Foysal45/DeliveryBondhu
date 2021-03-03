package com.ajkerdeal.app.essential.api.models.weight

import com.google.gson.annotations.SerializedName

data class WeightRangeDataModel(
        @SerializedName("id")
        var id: Int? = 0,
        @SerializedName("weight")
        var weight: String? = "",
        @SerializedName("type")
        var type: String? = "",
        @SerializedName("weightNumber")
        var weightNumber: Int? = 0,
        @SerializedName("expressTypeCourierDeliveryCharge")
        var expressTypeCourierDeliveryCharge: Double? = 0.0,
        @SerializedName("regularTypeCourierDeliveryCharge")
        var regularTypeCourierDeliveryCharge: Double? = 0.0
)
