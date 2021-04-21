package com.ajkerdeal.app.essential.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajkerdeal.app.essential.api.models.PagingModel
import com.ajkerdeal.app.essential.api.models.collection.CollectionData
import com.ajkerdeal.app.essential.api.models.collection.CollectionRequest
import com.ajkerdeal.app.essential.api.models.order.OrderCustomer
import com.ajkerdeal.app.essential.api.models.order.OrderRequest
import com.ajkerdeal.app.essential.api.models.status.FilterStatus
import com.ajkerdeal.app.essential.api.models.status.StatusUpdateModel
import com.ajkerdeal.app.essential.api.models.weight.UpdatePriceWithWeightRequest
import com.ajkerdeal.app.essential.repository.AppRepository
import com.ajkerdeal.app.essential.utils.SearchType
import com.ajkerdeal.app.essential.utils.SessionManager
import com.ajkerdeal.app.essential.utils.ViewState
import com.ajkerdeal.app.essential.utils.exhaustive
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class HomeViewModel(private val repository: AppRepository) : ViewModel() {

    val progress = MutableLiveData<Boolean>()
    val viewState = MutableLiveData<ViewState>(ViewState.NONE)
    val pagingState = MutableLiveData<PagingModel<MutableList<OrderCustomer>>>()
    val filterStatusList = MutableLiveData<MutableList<FilterStatus>>()

    fun loadOrderOrSearch(index: Int = 0, count: Int = 20, flag: Int = 0, statusId: String = "-1", dtStatusId: String = "-1", searchKey: String = "-1", type: SearchType = SearchType.None, serviceType: String, customType: String) {

        val requestBody = OrderRequest(SessionManager.userId.toString(), index, count, flag = flag, statusId = statusId, dtStatusId = dtStatusId, serviceType = serviceType, customType = customType, riderType = SessionManager.riderType)
        when (type) {
            is SearchType.Product -> requestBody.productTitle = searchKey
            is SearchType.Order -> requestBody.orderId = searchKey
            is SearchType.None -> {
                requestBody.orderId = "-1"
                requestBody.productTitle = "-1"
            }
        }
        viewState.value = ViewState.ProgressState(true, 1)
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.loadOrderList(requestBody)
            withContext(Dispatchers.Main) {
                viewState.value = ViewState.ProgressState(false, 1)
                when (response) {
                    is NetworkResponse.Success -> {
                        //Timber.d("${response.body}")
                        if (response.body != null) {
                            if (response.body.data != null) {
                                if (response.body.data?.customerOrderList.isNullOrEmpty()) {
                                    viewState.value = ViewState.EmptyViewState()
                                } else {
                                    if (index == 0) {
                                        Timber.d("Init data loaded")
                                        pagingState.value = PagingModel(true, response.body.data!!.totalCount, response.body.data!!.customerOrderList!!.toMutableList())
                                    } else {
                                        pagingState.value = PagingModel(false, response.body.data!!.totalCount, response.body.data!!.customerOrderList!!.toMutableList())
                                    }
                                }
                            } else {
                                viewState.value = ViewState.EmptyViewState()
                            }
                        } else {
                            viewState.value = ViewState.EmptyViewState()
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
                }.exhaustive
            }

        }
    }

    fun loadOrderOrSearchAD(index: Int = 0, count: Int = 20, flag: Int = 0, statusId: String = "-1", dtStatusId: String = "-1", searchKey: String = "-1", type: SearchType = SearchType.None, serviceType: String, customType: String) {

        val requestBody = OrderRequest(SessionManager.userId.toString(), index, count, flag = flag, statusId = statusId, dtStatusId = dtStatusId, serviceType = serviceType, customType = customType, riderType = SessionManager.riderType)
        when (type) {
            is SearchType.Product -> requestBody.productTitle = searchKey
            is SearchType.Order -> requestBody.orderId = searchKey
            is SearchType.None -> {
                requestBody.orderId = "-1"
                requestBody.productTitle = "-1"
            }
        }
        viewState.value = ViewState.ProgressState(true, 1)
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.loadOrderListAD(requestBody)
            withContext(Dispatchers.Main) {
                viewState.value = ViewState.ProgressState(false, 1)
                when (response) {
                    is NetworkResponse.Success -> {
                        //Timber.d("${response.body}")
                        if (response.body != null) {
                            if (response.body.data != null) {
                                if (response.body.data?.customerOrderList.isNullOrEmpty()) {
                                    viewState.value = ViewState.EmptyViewState()
                                } else {
                                    if (index == 0) {
                                        Timber.d("Init data loaded")
                                        pagingState.value = PagingModel(true, response.body.data!!.totalCount, response.body.data!!.customerOrderList!!.toMutableList())
                                    } else {
                                        pagingState.value = PagingModel(false, response.body.data!!.totalCount, response.body.data!!.customerOrderList!!.toMutableList())
                                    }
                                }
                            } else {
                                viewState.value = ViewState.EmptyViewState()
                            }
                        } else {
                            viewState.value = ViewState.EmptyViewState()
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
                }.exhaustive
            }

        }
    }

    fun updateOrderStatus(requestBody: MutableList<StatusUpdateModel>): LiveData<Boolean> {

        //val requestBody: MutableList<StatusUpdateModel> = mutableListOf()
        //val model = StatusUpdateModel()
        //requestBody.add(model)
        val responseLive = MutableLiveData<Boolean>()

        viewState.value = ViewState.ProgressState(true)
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.orderStatusUpdate(requestBody)
            withContext(Dispatchers.Main) {
                viewState.value = ViewState.ProgressState(false)
                when (response) {
                    is NetworkResponse.Success -> {
                        if (response.body.isSuccess) {
                            //val message = "স্টেটাস আপডেট হয়েছে"
                            val message = response.body.message
                            viewState.value = ViewState.ShowMessage(message)
                            //loadOrderOrSearch()
                            responseLive.value = true
                        } else {
                            viewState.value = ViewState.ShowMessage(response.body.message)
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
                }.exhaustive
            }

        }
        return responseLive
    }

    fun loadFilterStatus(serviceType: String): LiveData<MutableList<FilterStatus>> {

        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.loadFilterStatus(serviceType)
            withContext(Dispatchers.Main) {
                when (response) {
                    is NetworkResponse.Success -> {
                        filterStatusList.value = response.body.data!!
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
        return filterStatusList
    }

    fun loadCollectionList(couponId: Int, collectionPointId: Int): LiveData<List<CollectionData>> {

        val responseLive = MutableLiveData<List<CollectionData>>()
        viewState.value = ViewState.ProgressState(true)
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.loadCollectionList(CollectionRequest(couponId, SessionManager.userId, collectionPointId))
            withContext(Dispatchers.Main) {
                when (response) {
                    is NetworkResponse.Success -> {
                        viewState.value = ViewState.ProgressState(false)
                        if (response.body.data != null) {
                            responseLive.value = response.body.data!!
                        } else {
                            responseLive.value = listOf()
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
                }.exhaustive
            }

        }
        return responseLive
    }

    fun updatePriceWithWeight(requestBody: UpdatePriceWithWeightRequest): MutableLiveData<Boolean> {

        val responseBody = MutableLiveData<Boolean>()

        viewState.value = ViewState.ProgressState(true)
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.isUpdatePriceWithWeight(requestBody)
            withContext(Dispatchers.Main) {
                viewState.value = ViewState.ProgressState(false)
                when (response) {
                    is NetworkResponse.Success -> {
                        responseBody.value = response.body.model > 0
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
        return responseBody
    }


}