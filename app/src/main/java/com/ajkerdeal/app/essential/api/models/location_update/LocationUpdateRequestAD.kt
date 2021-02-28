package com.ajkerdeal.app.essential.api.models.location_update

import com.google.gson.annotations.SerializedName

data class LocationUpdateRequestAD(
        @SerializedName("ProfileId")
        var profileId: Int? = 0,
        @SerializedName("Latitude")
        var latitude: String? = "0.0",
        @SerializedName("Longitude")
        var Longitude: String? = "0.0"
)
