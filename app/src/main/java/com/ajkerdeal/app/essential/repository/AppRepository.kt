package com.ajkerdeal.app.essential.repository

import com.ajkerdeal.app.essential.api.ApiInterfaceADCORE
import com.ajkerdeal.app.essential.api.ApiInterfaceADM
import com.ajkerdeal.app.essential.api.ApiInterfaceANA
import com.ajkerdeal.app.essential.api.ApiInterfaceAPI
import com.ajkerdeal.app.essential.api.models.auth.LoginRequest
import com.ajkerdeal.app.essential.api.models.auth.fcm.UpdateTokenRequest
import com.ajkerdeal.app.essential.api.models.auth.otp.OTPSendRequest
import com.ajkerdeal.app.essential.api.models.auth.reset_password.CheckMobileRequest
import com.ajkerdeal.app.essential.api.models.auth.reset_password.UpdatePasswordRequest
import com.ajkerdeal.app.essential.api.models.auth.signup.SignUpRequest
import com.ajkerdeal.app.essential.api.models.collection.CollectionRequest
import com.ajkerdeal.app.essential.api.models.location_log.LocationLogRequest
import com.ajkerdeal.app.essential.api.models.location_update.LocationUpdateRequestAD
import com.ajkerdeal.app.essential.api.models.location_update.LocationUpdateRequestDT
import com.ajkerdeal.app.essential.api.models.merchant_ocation.MerchantLocationRequest
import com.ajkerdeal.app.essential.api.models.order.OrderRequest
import com.ajkerdeal.app.essential.api.models.pod.PodOrderRequest
import com.ajkerdeal.app.essential.api.models.profile.ProfileData
import com.ajkerdeal.app.essential.api.models.quick_order.QuickOrderUpdateRequest
import com.ajkerdeal.app.essential.api.models.quick_order.delivery_charge.DeliveryChargeRequest
import com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request.QuickOrderRequest
import com.ajkerdeal.app.essential.api.models.status.DTStatusUpdateModel
import com.ajkerdeal.app.essential.api.models.status.StatusUpdateModel
import com.ajkerdeal.app.essential.api.models.status_location.StatusLocationRequest
import com.ajkerdeal.app.essential.api.models.update_doc.UpdateDocRequest
import com.ajkerdeal.app.essential.api.models.user_status.LocationUpdateRequest
import com.ajkerdeal.app.essential.api.models.weight.UpdatePriceWithWeightRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AppRepository(private val apiInterfaceAPI: ApiInterfaceAPI, private val apiInterfaceANA: ApiInterfaceANA, private val apiInterfaceADM: ApiInterfaceADM, private val apiInterfaceADCORE: ApiInterfaceADCORE) {

    suspend fun features() = apiInterfaceAPI.features()

    suspend fun authUser(requestBody: LoginRequest) = apiInterfaceAPI.login(requestBody)

    suspend fun dtLogin(requestBody: LoginRequest) = apiInterfaceADCORE.dtLogin(requestBody)

    suspend fun signUpUser(requestBody: SignUpRequest) = apiInterfaceAPI.signUp(requestBody)

    suspend fun deliveryManRegistration(requestBody: SignUpRequest) = apiInterfaceADCORE.deliveryManRegistration(requestBody)

    suspend fun checkMobileNumber(requestBody: CheckMobileRequest) = apiInterfaceAPI.checkMobileNumber(requestBody)

    suspend fun sendOTP(requestBody: OTPSendRequest) = apiInterfaceAPI.sendOTP(requestBody)

    suspend fun verifyOTP(customerId: String, OTPCode: String) = apiInterfaceAPI.verifyOTP(customerId, OTPCode)

    suspend fun updatePassword(requestBody: UpdatePasswordRequest) = apiInterfaceAPI.updatePassword(requestBody)

    suspend fun updateFirebaseToken(requestBody: UpdateTokenRequest) = apiInterfaceAPI.updateFirebaseToken(requestBody)

    suspend fun updateUserStatus(userId: Int, isActive: String, flag: Int) = apiInterfaceAPI.updateUserStatus(userId, isActive, flag)

    suspend fun updateUserLocation(requestBody: LocationUpdateRequest) = apiInterfaceAPI.updateUserLocation(requestBody)

    suspend fun loadProfile(userId: Int) = apiInterfaceAPI.loadProfile(userId)

    suspend fun updateProfile(requestBody: ProfileData) = apiInterfaceAPI.updateProfile(requestBody)

    suspend fun updateProfile(requestBody: RequestBody, file1: MultipartBody.Part? = null, file2: MultipartBody.Part?, file3: MultipartBody.Part?) =
        apiInterfaceAPI.updateProfile(requestBody, file1, file2, file3)

    suspend fun loadFilterStatus(serviceType: String) = apiInterfaceAPI.loadFilterStatus(serviceType)

    suspend fun loadOrderListAD(requestBody: OrderRequest) = apiInterfaceAPI.loadOrderListAD(requestBody)

    suspend fun orderStatusUpdate(requestBody: List<StatusUpdateModel>) = apiInterfaceAPI.updateStatus(requestBody)

    suspend fun updateMerchantLocation(requestBody: MerchantLocationRequest) = apiInterfaceAPI.updateMerchantLocation(requestBody)

    suspend fun updateLocationUpdateRequestAD(requestBody: LocationUpdateRequestAD) = apiInterfaceAPI.updateLocationUpdateRequestAD(requestBody)

    suspend fun updateStatusChangeLocation(requestBody: StatusLocationRequest) = apiInterfaceAPI.updateStatusChangeLocation(requestBody)

    suspend fun loadOrderPodWiseList(requestBody: PodOrderRequest) = apiInterfaceAPI.loadOrderPodWiseList(requestBody)

    suspend fun loadCollectionList(requestBody: CollectionRequest) = apiInterfaceAPI.loadCollectionList(requestBody)

    suspend fun getDistrictList(id: Int) = apiInterfaceAPI.getDistrictList(id)

    suspend fun updateDocumentUrl(requestBody: UpdateDocRequest) = apiInterfaceAPI.updateDocumentUrl(requestBody)

    //########################################### Analytics ###############################################//

    suspend fun logRiderLocation(requestBody: LocationLogRequest) = apiInterfaceANA.logRiderLocation(requestBody)


    //########################################### ADM ###############################################//

    suspend fun imageUpload(
        imageUrl: RequestBody,
        fileName: RequestBody,
        file: MultipartBody.Part?
    ) = apiInterfaceADM.imageUpload(imageUrl, fileName, file)

    //########################################### ADCORE ###############################################//

    suspend fun updateLocationUpdateRequestDT(requestBody: LocationUpdateRequestDT) = apiInterfaceADCORE.updateLocationUpdateRequestDT(requestBody)

    suspend fun fetchWeightRange() = apiInterfaceADCORE.fetchWeightRange()

    suspend fun isUpdatePriceWithWeight(requestBody: UpdatePriceWithWeightRequest) = apiInterfaceADCORE.isUpdatePriceWithWeight(requestBody)

    suspend fun loadOrderListDT(requestBody: OrderRequest) = apiInterfaceADCORE.loadOrderListDT(requestBody)

    suspend fun updateStatusDT(requestBody: List<DTStatusUpdateModel>) = apiInterfaceADCORE.updateStatusDT(requestBody)

    //Quick Order
    suspend fun loadAllDistrictsById(id: Int) = apiInterfaceADCORE.loadAllDistrictsById(id)

    suspend fun checkIsQuickOrder(id: String) = apiInterfaceADCORE.checkIsQuickOrder(id)

    suspend fun updateQuickOrder(requestBody: QuickOrderUpdateRequest) = apiInterfaceADCORE.updateQuickOrder(requestBody)

    suspend fun getQuickOrders(requestBody: QuickOrderRequest) = apiInterfaceADCORE.getQuickOrders(requestBody)

    suspend fun getDeliveryCharge(requestBody: DeliveryChargeRequest) = apiInterfaceADCORE.getDeliveryCharge(requestBody)

    suspend fun fetchQuickOrderStatus() = apiInterfaceADCORE.fetchQuickOrderStatus()

}