package com.ajkerdeal.app.essential.api.models.charge


import com.google.gson.annotations.SerializedName

data class BreakableChargeData(
    @SerializedName("id")
    var id: Int,
    @SerializedName("breakableCharge")
    var breakableCharge: Double,
    @SerializedName("codChargePercentage")
    var codChargePercentage: Double,
    @SerializedName("codChargeDhakaPercentage")
    var codChargeDhakaPercentage: Double,
    @SerializedName("codChargeMin")
    var codChargeMin: Int,
    @SerializedName("bigProductCharge")
    var bigProductCharge: Double
)