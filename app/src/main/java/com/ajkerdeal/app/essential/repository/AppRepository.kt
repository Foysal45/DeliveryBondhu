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
import com.ajkerdeal.app.essential.api.models.auth.reset_password.UpdatePasswordRequestDT
import com.ajkerdeal.app.essential.api.models.auth.signup.SignUpRequest
import com.ajkerdeal.app.essential.api.models.collection.CollectionRequest
import com.ajkerdeal.app.essential.api.models.district.DistrictData
import com.ajkerdeal.app.essential.api.models.location_log.LocationLogRequest
import com.ajkerdeal.app.essential.api.models.location_update.LocationUpdateRequestAD
import com.ajkerdeal.app.essential.api.models.location_update.LocationUpdateRequestDT
import com.ajkerdeal.app.essential.api.models.merchant_ocation.MerchantLocationRequest
import com.ajkerdeal.app.essential.api.models.order.AcceptStatusRequestDT
import com.ajkerdeal.app.essential.api.models.order.OrderRequest
import com.ajkerdeal.app.essential.api.models.pod.PodOrderRequest
import com.ajkerdeal.app.essential.api.models.profile.ProfileData
import com.ajkerdeal.app.essential.api.models.profile.profile_DT.ProfileDataDT
import com.ajkerdeal.app.essential.api.models.quick_order.QuickOrderUpdateRequest
import com.ajkerdeal.app.essential.api.models.quick_order.delivery_charge.DeliveryChargeRequest
import com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request.QuickOrderRequest
import com.ajkerdeal.app.essential.api.models.quick_order.time_slot.TimeSlotRequest
import com.ajkerdeal.app.essential.api.models.quick_order_status.QuickOrderStatusUpdateRequest
import com.ajkerdeal.app.essential.api.models.status.DTStatusUpdateModel
import com.ajkerdeal.app.essential.api.models.status.StatusUpdateModel
import com.ajkerdeal.app.essential.api.models.status_location.StatusLocationRequest
import com.ajkerdeal.app.essential.api.models.update_doc.UpdateDocRequest
import com.ajkerdeal.app.essential.api.models.update_doc.UpdateDocRequestDT
import com.ajkerdeal.app.essential.api.models.user_status.LocationUpdateRequest
import com.ajkerdeal.app.essential.api.models.weight.UpdatePriceWithWeightRequest
import com.ajkerdeal.app.essential.database.AppDatabase
import com.ajkerdeal.app.essential.database.dao.DistrictDao
import com.ajkerdeal.app.essential.database.dao.NotificationDao
import com.ajkerdeal.app.essential.fcm.FCMData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body

class AppRepository(
    private val apiInterfaceAPI: ApiInterfaceAPI,
    private val apiInterfaceANA: ApiInterfaceANA,
    private val apiInterfaceADM: ApiInterfaceADM,
    private val apiInterfaceADCORE: ApiInterfaceADCORE,
    private val database: AppDatabase
    ) {

    //#region AppDatabase
    private val notificationDao: NotificationDao = database.notificationDao()
    private val districtDao: DistrictDao = database.districtDao()

    //#region NotificationDao
    suspend fun insert(model: FCMData): Long {
        return if (model.uid == 0) {
            notificationDao.upsert(model)
        } else {
            updateNotification(model).toLong()
        }
    }

    private suspend fun updateNotification(model: FCMData): Int = notificationDao.updateNotification(model)

    suspend fun getAllNotification() = notificationDao.getAllNotification()

    fun getAllNotificationFlow() = notificationDao.getAllNotificationFlow()

    suspend fun getNotificationById(id: Int) = notificationDao.getNotificationById(id)

    suspend fun deleteNotificationById(id: Int) = notificationDao.deleteNotificationById(id)

    suspend fun deleteAllNotification() = notificationDao.deleteAllNotification()
    //#endregion

    //#region DistrictDao
    suspend fun insert(list: List<DistrictData>): List<Long> {
        return districtDao.insertAll(list)
    }

    suspend fun insert(model: DistrictData): Long {
        return if (model.uid == 0) {
            districtDao.upsert(model)
        } else {
            updateDistrict(model).toLong()
        }
    }

    private suspend fun updateDistrict(model: DistrictData): Int = districtDao.updateDistrict(model)

    suspend fun getAllDistrict() = districtDao.getAllDistrict()

    suspend fun getDistrictById(districtId: Int) = districtDao.getDistrictById(districtId)

    suspend fun getDistrictByParentId(parentId: Int) = districtDao.getDistrictByParentId(parentId)

    suspend fun deleteDistrictById(id: Int) = districtDao.deleteDistrictById(id)

    suspend fun deleteAllDistrict() = districtDao.deleteAllDistrict()
    //#endregion
    //#endregion

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

    suspend fun updateDeliveryManInfo(requestBody: ProfileDataDT) = apiInterfaceADCORE.updateDeliveryManInfo(requestBody)

    suspend fun checkAcceptStatus(requestBody: AcceptStatusRequestDT) = apiInterfaceADCORE.checkAcceptStatus(requestBody)

    //Quick Order
    suspend fun loadAllDistrictsById(id: Int) = apiInterfaceADCORE.loadAllDistrictsById(id)

    suspend fun loadAllDistricts() = apiInterfaceADCORE.loadAllDistricts()

    suspend fun fetchCollectionTimeSlot() = apiInterfaceADCORE.fetchCollectionTimeSlot()

    suspend fun checkIsQuickOrder(id: String) = apiInterfaceADCORE.checkIsQuickOrder(id)

    suspend fun updateQuickOrder(requestBody: QuickOrderUpdateRequest) = apiInterfaceADCORE.updateQuickOrder(requestBody)

    suspend fun getQuickOrders(requestBody: QuickOrderRequest) = apiInterfaceADCORE.getQuickOrders(requestBody)

    suspend fun getDeliveryCharge(requestBody: DeliveryChargeRequest) = apiInterfaceADCORE.getDeliveryCharge(requestBody)

    suspend fun fetchQuickOrderStatus() = apiInterfaceADCORE.fetchQuickOrderStatus()

    suspend fun isAcceptedQuickOrder(orderRequestId: Int) = apiInterfaceADCORE.isAcceptedQuickOrder(orderRequestId)

    suspend fun updateQuickOrderStatus(requestBody: List<QuickOrderStatusUpdateRequest>) = apiInterfaceADCORE.updateQuickOrderStatus(requestBody)

    suspend fun updateDocumentUrlDT(requestBody: List<UpdateDocRequestDT>) = apiInterfaceADCORE.updateDocumentUrlDT(requestBody)

    suspend fun updateUserStatusDT(bondhuId: Int, isNowOffline: String) = apiInterfaceADCORE.updateUserStatusDT(bondhuId, isNowOffline)

    suspend fun getUserStatusDT(bondhuId: Int) = apiInterfaceADCORE.getUserStatusDT(bondhuId)

    suspend fun getBreakableCharge() = apiInterfaceADCORE.getBreakableCharge()

    suspend fun updatePasswordDT(requestBody: UpdatePasswordRequestDT) = apiInterfaceADCORE.updatePasswordDT(requestBody)
}