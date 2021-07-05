package com.ajkerdeal.app.essential.api

import com.ajkerdeal.app.essential.api.models.ErrorResponse
import com.ajkerdeal.app.essential.api.models.GenericResponse
import com.ajkerdeal.app.essential.api.models.auth.LoginRequest
import com.ajkerdeal.app.essential.api.models.auth.LoginResponse
import com.ajkerdeal.app.essential.api.models.auth.signup.SignUpRequest
import com.ajkerdeal.app.essential.api.models.charge.BreakableChargeData
import com.ajkerdeal.app.essential.api.models.district.AllDistrictListsModel
import com.ajkerdeal.app.essential.api.models.weight.WeightRangeDataModel
import com.ajkerdeal.app.essential.api.models.location_update.LocationUpdateRequestDT
import com.ajkerdeal.app.essential.api.models.location_update.LocationUpdateResponseDT
import com.ajkerdeal.app.essential.api.models.order.OrderRequest
import com.ajkerdeal.app.essential.api.models.order.OrderResponse
import com.ajkerdeal.app.essential.api.models.profile.profile_DT.ProfileDataDT
import com.ajkerdeal.app.essential.api.models.quick_order.QuickOrderUpdateRequest
import com.ajkerdeal.app.essential.api.models.quick_order.QuickOrderResponse
import com.ajkerdeal.app.essential.api.models.quick_order.delivery_charge.DeliveryChargeRequest
import com.ajkerdeal.app.essential.api.models.quick_order.delivery_charge.DeliveryChargeResponse
import com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request.QuickOrderList
import com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request.QuickOrderRequest
import com.ajkerdeal.app.essential.api.models.quick_order_status.QuickOrderStatus
import com.ajkerdeal.app.essential.api.models.quick_order_status.QuickOrderStatusUpdateRequest
import com.ajkerdeal.app.essential.api.models.status.DTStatusUpdateModel
import com.ajkerdeal.app.essential.api.models.update_doc.UpdateDocRequestDT
import com.ajkerdeal.app.essential.api.models.weight.UpdatePriceWithWeightRequest
import com.haroldadmin.cnradapter.NetworkResponse
import retrofit2.Retrofit
import retrofit2.http.*

interface ApiInterfaceADCORE {

    companion object {
        operator fun invoke(retrofit: Retrofit): ApiInterfaceADCORE {
            return retrofit.create(ApiInterfaceADCORE::class.java)
        }
    }

    @POST("api/Bondhu/Login")
    suspend fun dtLogin(@Body requestBody: LoginRequest): NetworkResponse<GenericResponse<LoginResponse>, ErrorResponse>

    @POST("api/Bondhu/DeliveryManRegistration")
    suspend fun deliveryManRegistration(@Body requestBody: SignUpRequest): NetworkResponse<GenericResponse<Boolean>, ErrorResponse>

    @POST("api/Update/UpdatePickupLocationsForLatLong")
    suspend fun updateLocationUpdateRequestDT(@Body requestBody: LocationUpdateRequestDT): NetworkResponse<GenericResponse<LocationUpdateResponseDT>, ErrorResponse>

    @GET("api/Fetch/GetWeightRange")
    suspend fun fetchWeightRange(): NetworkResponse<GenericResponse<List<WeightRangeDataModel>>, ErrorResponse>

    @GET("api/Fetch/LoadAllDistrictsById/{id}")
    suspend fun loadAllDistrictsById(@Path("id") id: Int): NetworkResponse<GenericResponse<List<AllDistrictListsModel>>, ErrorResponse>

    @POST("api/Update/UpdatePriceWithWeight")
    suspend fun isUpdatePriceWithWeight(@Body requestBody: UpdatePriceWithWeightRequest): NetworkResponse<GenericResponse<Int>, ErrorResponse>

    @POST("api/Bondhu/LoadOrderForBondhuApp")
    suspend fun loadOrderListDT(@Body requestBody: OrderRequest): NetworkResponse<GenericResponse<OrderResponse>, ErrorResponse>

    @PUT("/api/Bondhu/UpdateBondhuOrder")
    suspend fun updateStatusDT(@Body requestBody: List<DTStatusUpdateModel>): NetworkResponse<GenericResponse<Boolean>, ErrorResponse>


    @PUT("api/Bondhu/UpdateDeliveryManInfo")
    suspend fun updateDeliveryManInfo(@Body requestBody: ProfileDataDT): NetworkResponse<GenericResponse<Boolean>, ErrorResponse>


    //Quick Order
    @GET("api/QuickOrder/CheckIsQuickOrder/{orderId}")
    suspend fun checkIsQuickOrder(@Path("orderId") orderId: String): NetworkResponse<GenericResponse<Boolean>, ErrorResponse>

    @PUT("/api/QuickOrder/UpdateOrderInfoForApp")
    suspend fun updateQuickOrder(@Body updateRequestBody: QuickOrderUpdateRequest): NetworkResponse<GenericResponse<QuickOrderResponse>, ErrorResponse>

    @POST("api/Bondhu/GetQuickOrders")
    suspend fun getQuickOrders(@Body requestBody: QuickOrderRequest): NetworkResponse<GenericResponse<List<QuickOrderList>>, ErrorResponse>

    @POST("api/Fetch/DeliveryChargeDetailsAreaWise")
    suspend fun getDeliveryCharge(@Body requestBody: DeliveryChargeRequest): NetworkResponse<GenericResponse<List<DeliveryChargeResponse>>, ErrorResponse>

    @GET("api/Bondhu/GetQuickOrderStatus")
    suspend fun fetchQuickOrderStatus(): NetworkResponse<GenericResponse<List<QuickOrderStatus>>, ErrorResponse>

    @GET("api/QuickOrder/IsAcceptedQuickOrder/{orderRequestId}")
    suspend fun isAcceptedQuickOrder(@Path("orderRequestId") orderRequestId: Int): NetworkResponse<GenericResponse<Boolean>, ErrorResponse>

    @PUT("api/Bondhu/UpdateOrderRequests")
    suspend fun updateQuickOrderStatus(@Body requestBody: List<QuickOrderStatusUpdateRequest>): NetworkResponse<GenericResponse<Int>, ErrorResponse>

    @GET("api/Fetch/GetBreakableCharge")
    suspend fun getBreakableCharge(): NetworkResponse<GenericResponse<BreakableChargeData>, ErrorResponse>

    @PUT("api/Bondhu/UpdateDocumentUrl")
    suspend fun updateDocumentUrlDT(@Body requestBody: List<UpdateDocRequestDT>): NetworkResponse<GenericResponse<Int>, ErrorResponse>

}