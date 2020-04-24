package com.ajkerdeal.app.essential.ui.auth

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajkerdeal.app.essential.api.models.auth.LoginRequest
import com.ajkerdeal.app.essential.repository.AppRepository
import com.ajkerdeal.app.essential.utils.ViewState
import com.ajkerdeal.app.essential.utils.exhaustive
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AppRepository): ViewModel() {


    val userId = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    val progress = MutableLiveData<Boolean>()
    val viewState = MutableLiveData<ViewState>(ViewState.NONE)

    fun onLoginClicked(view: View) {
        authUser()
    }

    private fun authUser() {

        viewState.value = ViewState.KeyboardState()
        if (!validate()) {
            return
        }

        progress.value = true
        viewModelScope.launch(Dispatchers.IO) {

            val response = repository.authUser(LoginRequest(userId.value, password.value))
            progress.postValue(false)
            when (response) {
                is NetworkResponse.Success -> {

                    val data = response.body.data
                    if (data != null && data.deliveryUserId != 0) {

                        viewState.postValue(ViewState.NextState())
                    } else {
                        viewState.postValue(ViewState.ShowMessage(data?.message))
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
                    val message = "কোথাও কোনো সমস্যা হচ্ছে"
                    viewState.postValue(ViewState.ShowMessage(message))
                }
            }.exhaustive
        }

    }

    private fun validate(): Boolean {

        if (userId.value.isNullOrEmpty() || userId.value?.length != 11) {
            val message = "আপনার সঠিক মোবাইল নাম্বার লিখুন"
            viewState.value = ViewState.ShowMessage(message)
            return false
        }

        if (password.value.isNullOrEmpty()) {
            val message = "আপনার পাসওয়ার্ড লিখুন"
            viewState.value = ViewState.ShowMessage(message)
            return false
        }

        return true
    }
}