package com.ajkerdeal.app.essential.api

import com.ajkerdeal.app.essential.api.models.ErrorResponse
import com.ajkerdeal.app.essential.api.models.ResponseHeader
import com.ajkerdeal.app.essential.api.models.auth.LoginRequest
import com.ajkerdeal.app.essential.api.models.auth.LoginResponse
import com.ajkerdeal.app.essential.api.models.auth.fcm.UpdateTokenRequest
import com.ajkerdeal.app.essential.api.models.auth.otp.OTPSendRequest
import com.ajkerdeal.app.essential.api.models.auth.reset_password.CheckMobileRequest
import com.ajkerdeal.app.essential.api.models.auth.reset_password.UpdatePasswordRequest
import com.ajkerdeal.app.essential.api.models.auth.signup.SignUpRequest
import com.ajkerdeal.app.essential.api.models.auth.signup.SignUpResponse
import com.ajkerdeal.app.essential.api.models.collection.CollectionData
import com.ajkerdeal.app.essential.api.models.collection.CollectionRequest
import com.ajkerdeal.app.essential.api.models.location.LocationResponse
import com.ajkerdeal.app.essential.api.models.merchant_ocation.MerchantLocationRequest
import com.ajkerdeal.app.essential.api.models.order.OrderRequest
import com.ajkerdeal.app.essential.api.models.order.OrderResponse
import com.ajkerdeal.app.essential.api.models.pod.PodOrderRequest
import com.ajkerdeal.app.essential.api.models.pod.PodOrderResponse
import com.ajkerdeal.app.essential.api.models.profile.ProfileData
import com.ajkerdeal.app.essential.api.models.status.FilterStatus
import com.ajkerdeal.app.essential.api.models.status.StatusUpdateModel
import com.ajkerdeal.app.essential.api.models.status.StatusUpdateResponse
import com.ajkerdeal.app.essential.api.models.status_location.StatusLocationRequest
import com.ajkerdeal.app.essential.api.models.user_status.LocationUpdateRequest
import com.ajkerdeal.app.essential.api.models.user_status.UserStatus
import com.haroldadmin.cnradapter.NetworkResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.http.*

interface ApiInterface {

    companion object {
        operator fun invoke(retrofit: Retrofit): ApiInterface {
            return retrofit.create(ApiInterface::class.java)
        }
    }

    @POST("api/SelfDelivery/Login")
    suspend fun login(@Body requestBody: LoginRequest): NetworkResponse<ResponseHeader<LoginResponse>, ErrorResponse>

    @POST("api/SelfDelivery/SignUp")
    suspend fun signUp(@Body requestBody: SignUpRequest): NetworkResponse<ResponseHeader<SignUpResponse>, ErrorResponse>

    @POST("api/SelfDelivery/SearchByMobileNo")
    suspend fun checkMobileNumber(@Body requestBody: CheckMobileRequest): NetworkResponse<ResponseHeader<LoginResponse>, ErrorResponse>

    @POST("Recover/RetrivePassword/deliverybondhu")
    suspend fun sendOTP(@Body requestBody: OTPSendRequest): NetworkResponse<ResponseHeader<String>, ErrorResponse>

    @GET("Recover/CheckOTP/{customerId}/{OTPCode}")
    suspend fun verifyOTP(@Path("customerId") customerId: String, @Path("OTPCode") OTPCode: String): NetworkResponse<ResponseHeader<Int>, ErrorResponse>

    @POST("api/SelfDelivery/UpdatePassword")
    suspend fun updatePassword(@Body requestBody: UpdatePasswordRequest): NetworkResponse<ResponseHeader<SignUpResponse>, ErrorResponse>

    @POST("api/SelfDelivery/UpdateFirebaseToken")
    suspend fun updateFirebaseToken(@Body requestBody: UpdateTokenRequest): NetworkResponse<ResponseHeader<SignUpResponse>, ErrorResponse>

    @GET("api/SelfDelivery/UserAccess/{userId}/{isActive}/{flag}")
    suspend fun updateUserStatus(@Path("userId") userId: Int, @Path("isActive") isActive: String, @Path("flag") flag: Int): NetworkResponse<ResponseHeader<UserStatus>, ErrorResponse>

    @POST("api/SelfDelivery/UserLatLag")
    suspend fun updateUserLocation(@Body requestBody: LocationUpdateRequest): NetworkResponse<ResponseHeader<Int>, ErrorResponse>

    @GET("api/SelfDelivery/LoadBondhuInfo/{userId}")
    suspend fun loadProfile(@Path("userId") userId: Int): NetworkResponse<ResponseHeader<ProfileData>, ErrorResponse>

    @POST("api/SelfDelivery/ProfileUpdate")
    suspend fun updateProfile(@Body requestBody: ProfileData): NetworkResponse<ResponseHeader<SignUpResponse>, ErrorResponse>

    @Multipart
    @POST("api/SelfDelivery/ProfileUpload")
    suspend fun updateProfile(@Part("Data") requestBody: RequestBody, @Part file1: MultipartBody.Part? = null, @Part file2: MultipartBody.Part? = null, @Part file3: MultipartBody.Part? = null): NetworkResponse<ResponseHeader<Boolean>, ErrorResponse>

    @GET("api/SelfDelivery/LoadStatusNew/{serviceType}")
    suspend fun loadFilterStatus(@Path("serviceType") serviceType: String): NetworkResponse<ResponseHeader<MutableList<FilterStatus>>, ErrorResponse>

    @POST("api/SelfDelivery/LoadOrderNew")
    suspend fun loadOrderList(@Body requestBody: OrderRequest): NetworkResponse<ResponseHeader<OrderResponse>, ErrorResponse>

    @POST("api/SelfDelivery/LoadOrderPodWise")
    suspend fun loadOrderPodWiseList(@Body requestBody: PodOrderRequest): NetworkResponse<ResponseHeader<PodOrderResponse>, ErrorResponse>

    @POST("api/SelfDelivery/LoadCollectionProduct")
    suspend fun loadCollectionList(@Body requestBody: CollectionRequest): NetworkResponse<ResponseHeader<List<CollectionData>>, ErrorResponse>

    @POST("api/SelfDelivery/MerchantLatLag")
    suspend fun updateMerchantLocation(@Body requestBody: MerchantLocationRequest): NetworkResponse<ResponseHeader<Int>, ErrorResponse>

    @POST("api/SelfDelivery/AddLatLag")
    suspend fun updateStatusChangeLocation(@Body requestBody: StatusLocationRequest): NetworkResponse<ResponseHeader<Int>, ErrorResponse>

    @POST("CustomerInfo/OrderStatusUpdateForDeliveryMan")
    suspend fun updateStatus(@Body requestBody: List<StatusUpdateModel>): NetworkResponse<StatusUpdateResponse, ErrorResponse>

    @GET("District/v3/LoadAllDistrictFromJson/{parentId}")
    suspend fun getDistrictList(@Path("parentId") distId: Int = 0): NetworkResponse<ResponseHeader<LocationResponse>, ErrorResponse>

}