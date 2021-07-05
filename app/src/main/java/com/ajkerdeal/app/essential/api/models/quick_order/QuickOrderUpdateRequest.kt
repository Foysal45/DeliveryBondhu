package com.ajkerdeal.app.essential.api.models.quick_order


import com.google.gson.annotations.SerializedName

data class QuickOrderUpdateRequest(
    @SerializedName("courierOrdersId")
    var courierOrdersId: String? = "",
    @SerializedName("orderRequestId")
    var orderRequestId: Int = 0,
    @SerializedName("merchantId")
    var merchantId: Int = 0,
    @SerializedName("status")
    var status: Int = 0,
    @SerializedName("comment")
    var comment: String = "",
    @SerializedName("districtId")
    var districtId: Int = 0,
    @SerializedName("thanaId")
    var thanaId: Int = 0,
    @SerializedName("areaId")
    var areaId: Int = 0,
    @SerializedName("collectAddressDistrictId")
    var collectAddressDistrictId: Int = 0,
    @SerializedName("collectAddressThanaId")
    var collectAddressThanaId: Int = 0,
    @SerializedName("deliveryRangeId")
    var deliveryRangeId: Int = 0,
    @SerializedName("deliveryUserId")
    var deliveryUserId: Int = 0,
    @SerializedName("weightRangeId")
    var weightRangeId: Int = 0,
    @SerializedName("weight")
    var weight: String? = "",
    @SerializedName("paymentType")
    var paymentType: String? = "",
    @SerializedName("serviceType")
    var serviceType: String? = "",
    @SerializedName("deliveryCharge")
    var deliveryCharge: Double = 0.0,
    @SerializedName("collectionTimeSlotId")
    var collectionTimeSlotId: Int = 0,
    @SerializedName("collectionTime")
    var collectionTime: String? = "",
    @SerializedName("quickOrderImageUrl")
    var quickOrderImageUrl: String? = "",
    @SerializedName("OrderType")
    var orderType: String = "",
    @SerializedName("CollectionAmount")
    var collectionAmount: Double = 0.0,
    @SerializedName("ActualPackagePrice")
    var actualPackagePrice: Double = 0.0,
    @SerializedName("CodCharge")
    var codCharge: Double = 0.0,
    @SerializedName("updatedBy")
    var updatedBy: Int = 0,
    @SerializedName("isConfirmedBy")
    var isConfirmedBy: String = "deliveryman"
)