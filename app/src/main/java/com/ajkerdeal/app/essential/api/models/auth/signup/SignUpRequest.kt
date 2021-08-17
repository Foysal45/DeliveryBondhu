package com.ajkerdeal.app.essential.api.models.auth.signup


import com.google.gson.annotations.SerializedName

data class SignUpRequest(
    @SerializedName("Name")
    var name: String? = "",
    @SerializedName("Mobile")
    var mobile: String? = "",
    @SerializedName("AlternativeMobile")
    var alterMobile: String? = "",
    @SerializedName("BkashMobileNumber")
    var bKashAccountNumber: String? = "",
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
    @SerializedName("DistrictName")
    var districtName: String? = "",
    @SerializedName("ThanaName")
    var thanaName: String? = "",
    @SerializedName("Version")
    var version: String? = "",
    @SerializedName("DtId")
    var dtId: Int = 0,

    @SerializedName("RegistrationFrom")
    var registrationFrom: String? = "android",
    @SerializedName("IsActive")
    var isActive: Int = 0
)