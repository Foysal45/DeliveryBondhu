package com.ajkerdeal.app.essential.api.models.auth.signup


import com.google.gson.annotations.SerializedName

data class SignUpRequest(
    @SerializedName("Name")
    var name: String? = "",
    @SerializedName("Mobile")
    var mobile: String? = "",
    @SerializedName("Password")
    var password: String? = "",
    @SerializedName("Address")
    var address: String? = "",
    @SerializedName("DistrictId")
    var districtId: Int = 0,
    @SerializedName("ThanaId")
    var thanaId: Int = 0,
    @SerializedName("PostCode")
    var postCode: Int = 0,
    @SerializedName("IsActive")
    var isActive: Int = 0
)