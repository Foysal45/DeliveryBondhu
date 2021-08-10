package com.ajkerdeal.app.essential.api.models.district

import java.util.*

data class LocationData(
    var id: Int = 0,
    var displayNameBangla: String? = "",
    var displayNameEng: String? = "",
    var displayPostalCode: String? = "",
    var searchKey: String = "", // lower case
    var isDeactivate: Boolean = false
) {

    companion object {
        fun from(model: DistrictData): LocationData {
            return LocationData(
                model.districtId,
                model.districtBng,
                model.district,
                model.postalCode,
                model.district?.lowercase(Locale.US) ?: "",
                model.isActiveForCorona
            )
        }
    }

}
