package com.ajkerdeal.app.essential.api

import com.ajkerdeal.app.essential.api.models.ErrorResponse
import com.ajkerdeal.app.essential.api.models.ResponseHeader
import com.ajkerdeal.app.essential.api.models.auth.LoginRequest
import com.ajkerdeal.app.essential.api.models.auth.LoginResponse
import com.ajkerdeal.app.essential.api.models.order.OrderRequest
import com.ajkerdeal.app.essential.api.models.order.OrderResponse
import com.ajkerdeal.app.essential.api.models.status.StatusUpdateModel
import com.ajkerdeal.app.essential.api.models.status.StatusUpdateResponse
import com.haroldadmin.cnradapter.NetworkResponse
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiInterface {

    companion object {
        operator fun invoke(retrofit: Retrofit): ApiInterface {
            return retrofit.create(ApiInterface::class.java)
        }
    }

    @POST("api/SelfDelivery/Login")
    suspend fun login(@Body requestBody: LoginRequest): NetworkResponse<ResponseHeader<LoginResponse>, ErrorResponse>

    @POST("api/SelfDelivery/LoadOrder")
    suspend fun loadOrderList(@Body requestBody: OrderRequest): NetworkResponse<ResponseHeader<OrderResponse>, ErrorResponse>

    @POST("CustomerInfo/ProcessCouponsByCustomer")
    suspend fun updateStatus(@Body requestBody: List<StatusUpdateModel>): NetworkResponse<StatusUpdateResponse, ErrorResponse>

}