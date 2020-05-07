package com.ajkerdeal.app.essential.repository

import com.ajkerdeal.app.essential.api.ApiInterface
import com.ajkerdeal.app.essential.api.models.auth.LoginRequest
import com.ajkerdeal.app.essential.api.models.order.OrderRequest
import com.ajkerdeal.app.essential.api.models.pod.PodOrderRequest
import com.ajkerdeal.app.essential.api.models.status.StatusUpdateModel

class AppRepository(private val apiInterface: ApiInterface) {

    suspend fun authUser(requestBody: LoginRequest) = apiInterface.login(requestBody)

    suspend fun loadFilterStatus() = apiInterface.loadFilterStatus()

    suspend fun loadOrderList(requestBody: OrderRequest) = apiInterface.loadOrderList(requestBody)

    suspend fun orderStatusUpdate(requestBody: List<StatusUpdateModel>) = apiInterface.updateStatus(requestBody)

    suspend fun loadOrderPodWiseList(requestBody: PodOrderRequest) = apiInterface.loadOrderPodWiseList(requestBody)
}