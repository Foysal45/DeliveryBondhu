package com.ajkerdeal.app.essential.api

import com.ajkerdeal.app.essential.api.models.ErrorResponse
import com.ajkerdeal.app.essential.api.models.GenericResponse
import com.ajkerdeal.app.essential.api.models.ResponseHeader
import com.ajkerdeal.app.essential.api.models.auth.LoginRequest
import com.ajkerdeal.app.essential.api.models.auth.LoginResponse
import com.ajkerdeal.app.essential.api.models.auth.signup.SignUpRequest
import com.ajkerdeal.app.essential.api.models.auth.signup.SignUpResponse
import com.ajkerdeal.app.essential.api.models.weight.WeightRangeDataModel
import com.ajkerdeal.app.essential.api.models.location_update.LocationUpdateRequestDT
import com.ajkerdeal.app.essential.api.models.location_update.LocationUpdateResponseDT
import com.ajkerdeal.app.essential.api.models.order.OrderRequest
import com.ajkerdeal.app.essential.api.models.order.OrderResponse
import com.ajkerdeal.app.essential.api.models.status.DTStatusUpdateModel
import com.ajkerdeal.app.essential.api.models.weight.UpdatePriceWithWeightRequest
import com.haroldadmin.cnradapter.NetworkResponse
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApiInterfaceADCORE {

    companion object {
        operator fun invoke(retrofit: Retrofit): ApiInterfaceADCORE {
            return retrofit.create(ApiInterfaceADCORE::class.java)
        }
    }

    @POST("api/Bondhu/Login")
    suspend fun dtLogin(@Body requestBody: LoginRequest): NetworkResponse<GenericResponse<LoginResponse>, ErrorResponse>

    @POST("api/Bondhu/DeliveryManRegistration")
    suspend fun deliveryManRegistration(@Body requestBody: SignUpRequest): NetworkResponse<GenericResponse<SignUpResponse>, ErrorResponse>

    @POST("api/Update/UpdatePickupLocationsForLatLong")
    suspend fun updateLocationUpdateRequestDT(@Body requestBody: LocationUpdateRequestDT): NetworkResponse<GenericResponse<LocationUpdateResponseDT>, ErrorResponse>

    @GET("api/Fetch/GetWeightRange")
    suspend fun fetchWeightRange(): NetworkResponse<GenericResponse<List<WeightRangeDataModel>>, ErrorResponse>

    @POST("api/Update/UpdatePriceWithWeight")
    suspend fun isUpdatePriceWithWeight(@Body requestBody: UpdatePriceWithWeightRequest): NetworkResponse<GenericResponse<Int>, ErrorResponse>

    @POST("api/Bondhu/LoadOrderForBondhuApp")
    suspend fun loadOrderListDT(@Body requestBody: OrderRequest): NetworkResponse<GenericResponse<OrderResponse>, ErrorResponse>

    @PUT("/api/Bondhu/UpdateBondhuOrder")
    suspend fun updateStatusDT(@Body requestBody: List<DTStatusUpdateModel>): NetworkResponse<GenericResponse<Boolean>, ErrorResponse>

}