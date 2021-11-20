package com.ajkerdeal.app.essential.api


import com.ajkerdeal.app.essential.api.models.fcm.FCMRequest
import com.ajkerdeal.app.essential.api.models.fcm.FCMResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiInterfaceFCM {

    companion object {
        operator fun invoke(retrofit: Retrofit): ApiInterfaceFCM {
            return retrofit.create(ApiInterfaceFCM::class.java)
        }
    }

    @Headers(
        value = ["Content-type:application/json"]
    )
    @POST("fcm/send")
    suspend fun sendPushNotifications(
        @Header("Authorization") authToken: String,
        @Body requestBody: FCMRequest
    ): Response<FCMResponse>
}