package com.ajkerdeal.app.essential.ui.quick_order_scan

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajkerdeal.app.essential.api.models.PagingModel
import com.ajkerdeal.app.essential.api.models.charge.BreakableChargeData
import com.ajkerdeal.app.essential.api.models.district.DistrictThanaAreaDataModel
import com.ajkerdeal.app.essential.api.models.order.OrderCustomer
import com.ajkerdeal.app.essential.api.models.quick_order.QuickOrderResponse
import com.ajkerdeal.app.essential.api.models.quick_order.QuickOrderUpdateRequest
import com.ajkerdeal.app.essential.api.models.quick_order.delivery_charge.DeliveryChargeRequest
import com.ajkerdeal.app.essential.api.models.quick_order.delivery_charge.DeliveryChargeResponse
import com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request.QuickOrderList
import com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request.QuickOrderRequest
import com.ajkerdeal.app.essential.api.models.quick_order_status.QuickOrderStatus
import com.ajkerdeal.app.essential.api.models.quick_order_status.QuickOrderStatusUpdateRequest
import com.ajkerdeal.app.essential.repository.AppRepository
import com.ajkerdeal.app.essential.utils.ViewState
import com.ajkerdeal.app.essential.utils.exhaustive
import com.haroldadmin.cnradapter.NetworkResponse
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.File

class QuickOrderViewModel(private val repository: AppRepository): ViewModel() {

    val viewState = MutableLiveData<ViewState>(ViewState.NONE)
    val pagingState = MutableLiveData<PagingModel<MutableList<QuickOrderList>>>()

    fun fetchQuickOrderStatus(): LiveData<List<QuickOrderStatus>> {

        val responseData: MutableLiveData<List<QuickOrderStatus>> = MutableLiveData()
        viewState.value = ViewState.ProgressState(true)
        viewModelScope.launch(Dispatchers.IO){
            val response = repository.fetchQuickOrderStatus()
            withContext(Dispatchers.Main) {
                viewState.value = ViewState.ProgressState(false)
                when (response) {
                    is NetworkResponse.Success -> {
                        responseData.value = response.body.model
                    }
                    is NetworkResponse.ServerError -> {
                        val message = "দুঃখিত, এই মুহূর্তে আমাদের সার্ভার কানেকশনে সমস্যা হচ্ছে, কিছুক্ষণ পর আবার চেষ্টা করুন"
                        viewState.value = ViewState.ShowMessage(message)
                    }
                    is NetworkResponse.NetworkError -> {
                        val message = "দুঃখিত, এই মুহূর্তে আপনার ইন্টারনেট কানেকশনে সমস্যা হচ্ছে"
                        viewState.value = ViewState.ShowMessage(message)
                    }
                    is NetworkResponse.UnknownError -> {
                        val message = "কোথাও কোনো সমস্যা হচ্ছে, আবার চেষ্টা করুন"
                        viewState.value = ViewState.ShowMessage(message)
                        Timber.d(response.error)
                    }
                }.exhaustive
            }
        }
        return responseData
    }

    fun getQuickOrders(requestBody: QuickOrderRequest) {

        viewState.value = ViewState.ProgressState(true)
        viewModelScope.launch(Dispatchers.IO){
            val response = repository.getQuickOrders(requestBody)
            withContext(Dispatchers.Main) {
                viewState.value = ViewState.ProgressState(false)
                when (response) {
                    is NetworkResponse.Success -> {
                        val orderList = response.body.model
                        pagingState.value = PagingModel(true, 0, orderList.toMutableList())
                    }
                    is NetworkResponse.ServerError -> {
                        val message = "দুঃখিত, এই মুহূর্তে আমাদের সার্ভার কানেকশনে সমস্যা হচ্ছে, কিছুক্ষণ পর আবার চেষ্টা করুন"
                        viewState.value = ViewState.ShowMessage(message)
                    }
                    is NetworkResponse.NetworkError -> {
                        val message = "দুঃখিত, এই মুহূর্তে আপনার ইন্টারনেট কানেকশনে সমস্যা হচ্ছে"
                        viewState.value = ViewState.ShowMessage(message)
                    }
                    is NetworkResponse.UnknownError -> {
                        val message = "কোথাও কোনো সমস্যা হচ্ছে, আবার চেষ্টা করুন"
                        viewState.value = ViewState.ShowMessage(message)
                        Timber.d(response.error)
                    }
                }.exhaustive
            }
        }
    }

    fun updateQuickOrderStatus(requestBody: List<QuickOrderStatusUpdateRequest>): LiveData<Boolean> {
        val responseData: MutableLiveData<Boolean> = MutableLiveData()
        viewState.value = ViewState.ProgressState(true)
        viewModelScope.launch(Dispatchers.IO){
            val response = repository.updateQuickOrderStatus(requestBody)
            withContext(Dispatchers.Main) {
                viewState.value = ViewState.ProgressState(false)
                when (response) {
                    is NetworkResponse.Success -> {
                        responseData.value = response.body.model > 0
                    }
                    is NetworkResponse.ServerError -> {
                        val message = "দুঃখিত, এই মুহূর্তে আমাদের সার্ভার কানেকশনে সমস্যা হচ্ছে, কিছুক্ষণ পর আবার চেষ্টা করুন"
                        viewState.value = ViewState.ShowMessage(message)
                    }
                    is NetworkResponse.NetworkError -> {
                        val message = "দুঃখিত, এই মুহূর্তে আপনার ইন্টারনেট কানেকশনে সমস্যা হচ্ছে"
                        viewState.value = ViewState.ShowMessage(message)
                    }
                    is NetworkResponse.UnknownError -> {
                        val message = "কোথাও কোনো সমস্যা হচ্ছে, আবার চেষ্টা করুন"
                        viewState.value = ViewState.ShowMessage(message)
                        Timber.d(response.error)
                    }
                }.exhaustive
            }
        }
        return responseData
    }


    fun loadAllDistrictsById(id: Int): LiveData<List<DistrictThanaAreaDataModel>> {

        val responseData: MutableLiveData<List<DistrictThanaAreaDataModel>> = MutableLiveData()
        viewState.value = ViewState.ProgressState(true)
        viewModelScope.launch(Dispatchers.IO){
            val response = repository.loadAllDistrictsById(id)
            withContext(Dispatchers.Main) {
                viewState.value = ViewState.ProgressState(false)
                when (response) {
                    is NetworkResponse.Success -> {
                        responseData.value = response.body.model
                    }
                    is NetworkResponse.ServerError -> {
                        val message = "দুঃখিত, এই মুহূর্তে আমাদের সার্ভার কানেকশনে সমস্যা হচ্ছে, কিছুক্ষণ পর আবার চেষ্টা করুন"
                        viewState.value = ViewState.ShowMessage(message)
                    }
                    is NetworkResponse.NetworkError -> {
                        val message = "দুঃখিত, এই মুহূর্তে আপনার ইন্টারনেট কানেকশনে সমস্যা হচ্ছে"
                        viewState.value = ViewState.ShowMessage(message)
                    }
                    is NetworkResponse.UnknownError -> {
                        val message = "কোথাও কোনো সমস্যা হচ্ছে, আবার চেষ্টা করুন"
                        viewState.value = ViewState.ShowMessage(message)
                        Timber.d(response.error)
                    }
                }.exhaustive
            }
        }
        return responseData
    }

    fun getDeliveryCharge(requestBody: DeliveryChargeRequest): LiveData<List<DeliveryChargeResponse>> {

        val responseData: MutableLiveData<List<DeliveryChargeResponse>> = MutableLiveData()
        viewState.value = ViewState.ProgressState(true)
        viewModelScope.launch(Dispatchers.IO){
            val response = repository.getDeliveryCharge(requestBody)
            withContext(Dispatchers.Main) {
                viewState.value = ViewState.ProgressState(false)
                when (response) {
                    is NetworkResponse.Success -> {
                        responseData.value = response.body.model
                    }
                    is NetworkResponse.ServerError -> {
                        val message = "দুঃখিত, এই মুহূর্তে আমাদের সার্ভার কানেকশনে সমস্যা হচ্ছে, কিছুক্ষণ পর আবার চেষ্টা করুন"
                        viewState.value = ViewState.ShowMessage(message)
                    }
                    is NetworkResponse.NetworkError -> {
                        val message = "দুঃখিত, এই মুহূর্তে আপনার ইন্টারনেট কানেকশনে সমস্যা হচ্ছে"
                        viewState.value = ViewState.ShowMessage(message)
                    }
                    is NetworkResponse.UnknownError -> {
                        val message = "কোথাও কোনো সমস্যা হচ্ছে, আবার চেষ্টা করুন"
                        viewState.value = ViewState.ShowMessage(message)
                        Timber.d(response.error)
                    }
                }.exhaustive
            }
        }
        return responseData
    }

    fun checkIsQuickOrder(orderId: String): LiveData<Boolean> {

        val responseData: MutableLiveData<Boolean> = MutableLiveData()
        viewState.value = ViewState.ProgressState(true)
        viewModelScope.launch(Dispatchers.IO){
            val response = repository.checkIsQuickOrder(orderId)
            withContext(Dispatchers.Main) {
                viewState.value = ViewState.ProgressState(false)
                when (response) {
                    is NetworkResponse.Success -> {
                        responseData.value = response.body.model
                    }
                    is NetworkResponse.ServerError -> {
                        val message = "দুঃখিত, এই মুহূর্তে আমাদের সার্ভার কানেকশনে সমস্যা হচ্ছে, কিছুক্ষণ পর আবার চেষ্টা করুন"
                        viewState.value = ViewState.ShowMessage(message)
                    }
                    is NetworkResponse.NetworkError -> {
                        val message = "দুঃখিত, এই মুহূর্তে আপনার ইন্টারনেট কানেকশনে সমস্যা হচ্ছে"
                        viewState.value = ViewState.ShowMessage(message)
                    }
                    is NetworkResponse.UnknownError -> {
                        val message = "কোথাও কোনো সমস্যা হচ্ছে, আবার চেষ্টা করুন"
                        viewState.value = ViewState.ShowMessage(message)
                        Timber.d(response.error)
                    }
                }.exhaustive
            }
        }
        return responseData
    }

    fun uploadProfilePhoto(orderId: String, context: Context, url: String): LiveData<Boolean> {

        val responseData: MutableLiveData<Boolean> = MutableLiveData()
        viewState.value = ViewState.ProgressState(true, 1)
        val mediaTypeMultipart = "multipart/form-data".toMediaTypeOrNull()

        viewModelScope.launch(Dispatchers.IO) {

            val file = File(url)
            Timber.d("ImageLog ImageFilePath: $file")
            val compressedFile = Compressor.compress(context, file)
            val requestFile = compressedFile.asRequestBody(mediaTypeMultipart)
            val part = MultipartBody.Part.createFormData("", "${orderId}.jpg", requestFile)
            val imageUrl = "images/dt/orderrequest".toRequestBody()
            val fileName = "${orderId}.jpg".toRequestBody()
            val response = repository.imageUpload(imageUrl, fileName, part)

            withContext(Dispatchers.Main) {
                viewState.value = ViewState.ProgressState(false, 1)
                when (response) {
                    is NetworkResponse.Success -> {
                        responseData.value = response.body
                    }
                    is NetworkResponse.ServerError -> {
                        val message = "দুঃখিত, এই মুহূর্তে আমাদের সার্ভার কানেকশনে সমস্যা হচ্ছে, কিছুক্ষণ পর আবার চেষ্টা করুন"
                        viewState.value = ViewState.ShowMessage(message)
                    }
                    is NetworkResponse.NetworkError -> {
                        val message = "দুঃখিত, এই মুহূর্তে আপনার ইন্টারনেট কানেকশনে সমস্যা হচ্ছে"
                        viewState.value = ViewState.ShowMessage(message)
                    }
                    is NetworkResponse.UnknownError -> {
                        val message = "কোথাও কোনো সমস্যা হচ্ছে, আবার চেষ্টা করুন"
                        viewState.value = ViewState.ShowMessage(message)
                        Timber.d(response.error)
                    }
                }
            }
        }
        return responseData
    }

    fun updateQuickOrder(requestBody: QuickOrderUpdateRequest): LiveData<QuickOrderResponse> {

        val responseData: MutableLiveData<QuickOrderResponse> = MutableLiveData()
        viewState.value = ViewState.ProgressState(true, 1)
        viewModelScope.launch(Dispatchers.IO){
            val response = repository.updateQuickOrder(requestBody)
            withContext(Dispatchers.Main) {
                viewState.value = ViewState.ProgressState(false, 1)
                when (response) {
                    is NetworkResponse.Success -> {
                        responseData.value = response.body.model
                    }
                    is NetworkResponse.ServerError -> {
                        val message = "দুঃখিত, এই মুহূর্তে আমাদের সার্ভার কানেকশনে সমস্যা হচ্ছে, কিছুক্ষণ পর আবার চেষ্টা করুন"
                        viewState.value = ViewState.ShowMessage(message)
                    }
                    is NetworkResponse.NetworkError -> {
                        val message = "দুঃখিত, এই মুহূর্তে আপনার ইন্টারনেট কানেকশনে সমস্যা হচ্ছে"
                        viewState.value = ViewState.ShowMessage(message)
                    }
                    is NetworkResponse.UnknownError -> {
                        val message = "কোথাও কোনো সমস্যা হচ্ছে, আবার চেষ্টা করুন"
                        viewState.value = ViewState.ShowMessage(message)
                        Timber.d(response.error)
                    }
                }.exhaustive
            }
        }
        return responseData
    }

    fun getBreakableCharge(): LiveData<BreakableChargeData> {
        viewState.value = ViewState.ProgressState(true)
        val responseData = MutableLiveData<BreakableChargeData>()

        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.getBreakableCharge()
            withContext(Dispatchers.Main) {
                viewState.value = ViewState.ProgressState(false)
                when (response) {
                    is NetworkResponse.Success -> {
                        if (response.body.model != null) {
                            responseData.value = response.body.model!!
                        }
                    }
                    is NetworkResponse.ServerError -> {
                        val message = "দুঃখিত, এই মুহূর্তে আমাদের সার্ভার কানেকশনে সমস্যা হচ্ছে, কিছুক্ষণ পর আবার চেষ্টা করুন"
                        viewState.value = ViewState.ShowMessage(message)
                    }
                    is NetworkResponse.NetworkError -> {
                        val message = "দুঃখিত, এই মুহূর্তে আপনার ইন্টারনেট কানেকশনে সমস্যা হচ্ছে"
                        viewState.value = ViewState.ShowMessage(message)
                    }
                    is NetworkResponse.UnknownError -> {
                        val message = "কোথাও কোনো সমস্যা হচ্ছে, আবার চেষ্টা করুন"
                        viewState.value = ViewState.ShowMessage(message)
                        Timber.d(response.error)
                    }
                }
            }
        }
        return responseData
    }

}