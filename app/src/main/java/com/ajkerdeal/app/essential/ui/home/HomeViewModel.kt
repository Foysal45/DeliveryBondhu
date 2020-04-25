package com.ajkerdeal.app.essential.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajkerdeal.app.essential.api.models.PagingModel
import com.ajkerdeal.app.essential.api.models.order.OrderCustomer
import com.ajkerdeal.app.essential.api.models.order.OrderRequest
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

    fun loadOrderOrSearch(index: Int = 0, count: Int = 20, searchKey: String = "-1", type: SearchType = SearchType.None) {

        val requestBody = OrderRequest(SessionManager.userId.toString(), index, count)
        when (type) {
            is SearchType.Product -> requestBody.productTitle = searchKey
            is SearchType.Status -> requestBody.statusId = searchKey
        }

        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.loadOrderList(requestBody)
            when (response) {
                is NetworkResponse.Success -> {
                    Timber.d("${response.body}")
                    withContext(Dispatchers.Main) {

                        if (response.body != null) {
                            if (response.body.data != null) {
                                if (response.body.data?.customerOrderList.isNullOrEmpty()) {
                                    viewState.value = ViewState.EmptyViewState()
                                } else {
                                    if (index == 0) {
                                        Timber.d("Init data loaded")
                                        pagingState.value = PagingModel(true, response.body.data!!.customerOrderList!!.toMutableList())
                                    } else {
                                        pagingState.value = PagingModel(false, response.body.data!!.customerOrderList!!.toMutableList())
                                    }
                                }
                            } else {
                                viewState.value = ViewState.EmptyViewState()
                            }
                        } else {
                            viewState.setValue(ViewState.EmptyViewState())
                        }
                    }
                }
                is NetworkResponse.ServerError -> {
                    val message = "দুঃখিত, এই মুহূর্তে আমাদের সার্ভার কানেকশনে সমস্যা হচ্ছে, কিছুক্ষণ পর আবার চেষ্টা করুন"
                    viewState.postValue(ViewState.ShowMessage(message))

                }
                is NetworkResponse.NetworkError -> {
                    val message = "দুঃখিত, এই মুহূর্তে আপনার ইন্টারনেট কানেকশনে সমস্যা হচ্ছে"
                    viewState.postValue(ViewState.ShowMessage(message))
                }
                is NetworkResponse.UnknownError -> {
                    val message = "কোথাও কোনো সমস্যা হচ্ছে, আবার চেষ্টা করুন"
                    viewState.postValue(ViewState.ShowMessage(message))
                    Timber.d(response.error)
                }
            }.exhaustive
        }
    }

}