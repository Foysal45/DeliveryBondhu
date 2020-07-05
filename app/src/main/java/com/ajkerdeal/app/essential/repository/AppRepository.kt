package com.ajkerdeal.app.essential.repository

import com.ajkerdeal.app.essential.api.ApiInterface
import com.ajkerdeal.app.essential.api.models.auth.LoginRequest
import com.ajkerdeal.app.essential.api.models.auth.fcm.UpdateTokenRequest
import com.ajkerdeal.app.essential.api.models.auth.otp.OTPSendRequest
import com.ajkerdeal.app.essential.api.models.auth.reset_password.CheckMobileRequest
import com.ajkerdeal.app.essential.api.models.auth.reset_password.UpdatePasswordRequest
import com.ajkerdeal.app.essential.api.models.auth.signup.SignUpRequest
import com.ajkerdeal.app.essential.api.models.collection.CollectionRequest
import com.ajkerdeal.app.essential.api.models.merchant_ocation.MerchantLocationRequest
import com.ajkerdeal.app.essential.api.models.order.OrderRequest
import com.ajkerdeal.app.essential.api.models.pod.PodOrderRequest
import com.ajkerdeal.app.essential.api.models.profile.ProfileData
import com.ajkerdeal.app.essential.api.models.status.StatusUpdateModel
import com.ajkerdeal.app.essential.api.models.status_location.StatusLocationRequest
import com.ajkerdeal.app.essential.api.models.user_status.LocationUpdateRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AppRepository(private val apiInterface: ApiInterface) {

    suspend fun features() = apiInterface.features()

    suspend fun authUser(requestBody: LoginRequest) = apiInterface.login(requestBody)

    suspend fun signUpUser(requestBody: SignUpRequest) = apiInterface.signUp(requestBody)

    suspend fun checkMobileNumber(requestBody: CheckMobileRequest) = apiInterface.checkMobileNumber(requestBody)

    suspend fun sendOTP(requestBody: OTPSendRequest) = apiInterface.sendOTP(requestBody)

    suspend fun verifyOTP(customerId: String, OTPCode: String) = apiInterface.verifyOTP(customerId, OTPCode)

    suspend fun updatePassword(requestBody: UpdatePasswordRequest) = apiInterface.updatePassword(requestBody)

    suspend fun updateFirebaseToken(requestBody: UpdateTokenRequest) = apiInterface.updateFirebaseToken(requestBody)

    suspend fun updateUserStatus(userId: Int, isActive: String, flag: Int) = apiInterface.updateUserStatus(userId, isActive, flag)

    suspend fun updateUserLocation(requestBody: LocationUpdateRequest) = apiInterface.updateUserLocation(requestBody)

    suspend fun loadProfile(userId: Int) = apiInterface.loadProfile(userId)

    suspend fun updateProfile(requestBody: ProfileData) = apiInterface.updateProfile(requestBody)

    suspend fun updateProfile(requestBody: RequestBody, file1: MultipartBody.Part? = null, file2: MultipartBody.Part?, file3: MultipartBody.Part?) =
        apiInterface.updateProfile(requestBody, file1, file2, file3)

    suspend fun loadFilterStatus(serviceType: String) = apiInterface.loadFilterStatus(serviceType)

    suspend fun loadOrderList(requestBody: OrderRequest) = apiInterface.loadOrderList(requestBody)

    suspend fun orderStatusUpdate(requestBody: List<StatusUpdateModel>) = apiInterface.updateStatus(requestBody)

    suspend fun updateMerchantLocation(requestBody: MerchantLocationRequest) = apiInterface.updateMerchantLocation(requestBody)

    suspend fun updateStatusChangeLocation(requestBody: StatusLocationRequest) = apiInterface.updateStatusChangeLocation(requestBody)

    suspend fun loadOrderPodWiseList(requestBody: PodOrderRequest) = apiInterface.loadOrderPodWiseList(requestBody)

    suspend fun loadCollectionList(requestBody: CollectionRequest) = apiInterface.loadCollectionList(requestBody)

    suspend fun getDistrictList(id: Int) = apiInterface.getDistrictList(id)
}