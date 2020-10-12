package com.ajkerdeal.app.essential.api

import com.ajkerdeal.app.essential.api.models.ErrorResponse
import com.haroldadmin.cnradapter.NetworkResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiInterfaceADM {

    companion object {
        operator fun invoke(retrofit: Retrofit): ApiInterfaceADM {
            return retrofit.create(ApiInterfaceADM::class.java)
        }
    }

    @Multipart
    @POST("Image/ImageUploadForFile")
    suspend fun imageUpload(
        @Part("ImageUrl") imageUrl: RequestBody,
        @Part("FileName") fileName: RequestBody,
        @Part file: MultipartBody.Part? = null
    ): NetworkResponse<Boolean, ErrorResponse>

}