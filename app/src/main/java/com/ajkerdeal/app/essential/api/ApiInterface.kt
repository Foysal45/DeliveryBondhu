package com.ajkerdeal.app.essential.api

import com.ajkerdeal.app.essential.api.models.ErrorResponse
import com.ajkerdeal.app.essential.api.models.ResponseHeader
import com.ajkerdeal.app.essential.api.models.auth.LoginRequest
import com.ajkerdeal.app.essential.api.models.auth.LoginResponse
import com.ajkerdeal.app.essential.api.models.order.OrderRequest
import com.ajkerdeal.app.essential.api.models.order.OrderResponse
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

    @POST("SelfDelivery/Login")
    suspend fun login(@Body requestBody: LoginRequest): NetworkResponse<ResponseHeader<LoginResponse>, ErrorResponse>

    @POST("SelfDelivery/LoadOrder")
    suspend fun loadOrderList(@Body requestBody: OrderRequest): NetworkResponse<ResponseHeader<OrderResponse>, ErrorResponse>

    /*@POST("SelfDelivery/LoadOrder")
    fun loadOrderListTest(@Body requestBody: OrderRequest): Call<ResponseHeader<OrderResponse>>*/
}