package com.ajkerdeal.app.essential.repository

import com.ajkerdeal.app.essential.api.ApiInterface
import com.ajkerdeal.app.essential.api.models.auth.LoginRequest
import com.ajkerdeal.app.essential.api.models.order.OrderRequest

class AppRepository(private val apiInterface: ApiInterface) {

    suspend fun authUser(requestBody: LoginRequest) = apiInterface.login(requestBody)

    suspend fun loadOrderList(requestBody: OrderRequest) = apiInterface.loadOrderList(requestBody)

    //fun loadOrderListTest(requestBody: OrderRequest) = apiInterface.loadOrderListTest(requestBody)
}