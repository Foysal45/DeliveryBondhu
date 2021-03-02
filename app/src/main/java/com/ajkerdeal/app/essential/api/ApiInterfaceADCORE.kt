package com.ajkerdeal.app.essential.api

import com.ajkerdeal.app.essential.api.models.ErrorResponse
import com.ajkerdeal.app.essential.api.models.GenericResponse
import com.ajkerdeal.app.essential.api.models.collection.WeightRangeDataModel
import com.ajkerdeal.app.essential.api.models.location_update.LocationUpdateRequestDT
import com.ajkerdeal.app.essential.api.models.location_update.LocationUpdateResponseDT
import com.haroldadmin.cnradapter.NetworkResponse
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiInterfaceADCORE {

    companion object {
        operator fun invoke(retrofit: Retrofit): ApiInterfaceADCORE {
            return retrofit.create(ApiInterfaceADCORE::class.java)
        }
    }

    @POST("api/Update/UpdatePickupLocationsForLatLong")
    suspend fun updateLocationUpdateRequestDT(@Body requestBody: LocationUpdateRequestDT): NetworkResponse<GenericResponse<LocationUpdateResponseDT>, ErrorResponse>

    @GET("api/Fetch/GetWeightRange")
    suspend fun fetchWeightRange(): NetworkResponse<GenericResponse<List<WeightRangeDataModel>>, ErrorResponse>

}