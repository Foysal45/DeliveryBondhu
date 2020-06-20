package com.ajkerdeal.app.essential.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajkerdeal.app.essential.api.models.auth.fcm.UpdateTokenRequest
import com.ajkerdeal.app.essential.api.models.user_status.UserStatus
import com.ajkerdeal.app.essential.repository.AppRepository
import com.ajkerdeal.app.essential.utils.SessionManager
import com.ajkerdeal.app.essential.utils.exhaustive
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class HomeActivityViewModel(private val repository: AppRepository): ViewModel() {

    val isOfflineLive = MutableLiveData<Boolean>()

    fun clearFirebaseToken(userId: Int) {

        viewModelScope.launch (Dispatchers.IO) {
            repository.updateFirebaseToken(UpdateTokenRequest(userId, ""))
        }
    }

    fun updateUserStatus(isOffline: String = "true", flag: Int = 0): LiveData<UserStatus> {

        val responseData: MutableLiveData<UserStatus> = MutableLiveData()
        //viewState.value = ViewState.ProgressState(true)
        viewModelScope.launch(Dispatchers.IO){
            val response = repository.updateUserStatus(SessionManager.userId, isOffline, flag)
            withContext(Dispatchers.Main) {
                //viewState.value = ViewState.ProgressState(false)
                when (response) {
                    is NetworkResponse.Success -> {
                        responseData.value = response.body.data
                        isOfflineLive.value = response.body.data?.isNowOffline
                    }
                    is NetworkResponse.ServerError -> {
                        val message = "দুঃখিত, এই মুহূর্তে আমাদের সার্ভার কানেকশনে সমস্যা হচ্ছে, কিছুক্ষণ পর আবার চেষ্টা করুন"
                        //viewState.value = ViewState.ShowMessage(message)
                    }
                    is NetworkResponse.NetworkError -> {
                        val message = "দুঃখিত, এই মুহূর্তে আপনার ইন্টারনেট কানেকশনে সমস্যা হচ্ছে"
                        //viewState.value = ViewState.ShowMessage(message)
                    }
                    is NetworkResponse.UnknownError -> {
                        val message = "কোথাও কোনো সমস্যা হচ্ছে, আবার চেষ্টা করুন"
                        //viewState.value = ViewState.ShowMessage(message)
                        Timber.d(response.error)
                    }
                }.exhaustive
            }
        }
        return responseData
    }
}