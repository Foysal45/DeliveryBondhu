package com.ajkerdeal.app.essential.api.models.weight

import com.google.gson.annotations.SerializedName

data class UpdatePriceWithWeightRequest(
    @SerializedName("districtId")
    var districtId: Int? = 0,
    @SerializedName("thanaId")
    var thanaId: Int? = 0,
    @SerializedName("areaId")
    var areaId: Int? = 0,
    @SerializedName("weightRangeId")
    var weightRangeId: Int? = 0,
    @SerializedName("courierOrderId")
    var courierOrderId: String? = "",
    @SerializedName("deliveryRangeId")
    var deliveryRangeId: Int? = 0
)
