package com.ajkerdeal.app.essential.api

import com.ajkerdeal.app.essential.api.models.ErrorResponse
import com.ajkerdeal.app.essential.api.models.location_log.LocationLogRequest
import com.ajkerdeal.app.essential.api.models.location_log.LocationLogResponse
import com.haroldadmin.cnradapter.NetworkResponse
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST


interface ApiInterfaceANA {

    companion object {
        operator fun invoke(retrofit: Retrofit): ApiInterfaceANA {
            return retrofit.create(ApiInterfaceANA::class.java)
        }
    }

    @POST("api/riderlocation")
    suspend fun logRiderLocation(@Body requestBody: LocationLogRequest): NetworkResponse<LocationLogResponse, ErrorResponse>

}