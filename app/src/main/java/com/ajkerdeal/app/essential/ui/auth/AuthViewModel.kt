package com.ajkerdeal.app.essential.ui.auth

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajkerdeal.app.essential.api.models.auth.LoginRequest
import com.ajkerdeal.app.essential.api.models.auth.otp.OTPSendRequest
import com.ajkerdeal.app.essential.api.models.auth.signup.SignUpRequest
import com.ajkerdeal.app.essential.api.models.location.LocationResponse
import com.ajkerdeal.app.essential.repository.AppRepository
import com.ajkerdeal.app.essential.utils.SessionManager
import com.ajkerdeal.app.essential.utils.ViewState
import com.ajkerdeal.app.essential.utils.exhaustive
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class AuthViewModel(private val repository: AppRepository): ViewModel() {


    val userId = MutableLiveData<String>("01722335535")
    val password = MutableLiveData<String>("123")
    val confirmPassword = MutableLiveData<String>()
    val address = MutableLiveData<String>("")
    val districtId = MutableLiveData<Int>(0)
    val thanaId = MutableLiveData<Int>(0)
    val otpCode = MutableLiveData<String>("")

    val progress = MutableLiveData<Boolean>()
    val viewState = MutableLiveData<ViewState>(ViewState.NONE)
    private val _locationModel = MutableLiveData<LocationResponse>()

    fun onLoginClicked(view: View) {
        authUser()
    }

    fun onRegistrationClicked(view: View) {
        signUpUser()
    }

    fun onResetPassword(view: View) {
        sendOTP(userId.value ?: "")
    }

    fun onOTPSubmit(view: View) {
        verifyOTP(userId.value ?: "", otpCode.value ?: "")
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

                        SessionManager.createSession(data.deliveryUserId,data.deliveryUserName,data.mobileNumber,"")
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
                    val message = "কোথাও কোনো সমস্যা হচ্ছে, আবার চেষ্টা করুন"
                    viewState.postValue(ViewState.ShowMessage(message))
                    Timber.d(response.error)
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

    private fun signUpUser() {

        viewState.value = ViewState.KeyboardState()
        if (!validateSignUp()) {
            return
        }

        progress.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.signUpUser(SignUpRequest("", userId.value, password.value, address.value, districtId.value ?: 0, thanaId.value ?: 0))
            withContext(Dispatchers.Main) {
                progress.value = false
                when (response) {
                    is NetworkResponse.Success -> {

                        val data = response.body.data
                        if (data != null && data.customerId != 0) {
                            viewState.value = ViewState.ShowMessage(data.message)

                            sendOTP(userId.value ?: "")
                        } else {
                            viewState.value = ViewState.ShowMessage(data?.message)
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

    private fun validateSignUp(): Boolean {

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

        if (confirmPassword.value.isNullOrEmpty() || password.value != confirmPassword.value) {
            val message = "আপনার কনফার্ম পাসওয়ার্ড লিখুন"
            viewState.value = ViewState.ShowMessage(message)
            return false
        }

        if (districtId.value == 0) {
            val message = "জেলা নির্বাচন করুন"
            viewState.value = ViewState.ShowMessage(message)
            return false
        }

        if (thanaId.value == 0) {
            val message = "থানা নির্বাচন করুন"
            viewState.value = ViewState.ShowMessage(message)
            return false
        }

        return true
    }

    fun loadLocationList(id: Int = 0): LiveData<LocationResponse> {

        val locationModel = MutableLiveData<LocationResponse>()
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.getDistrictList(id)
            withContext(Dispatchers.Main) {
                when (response) {
                    is NetworkResponse.Success -> {
                        locationModel.value = response.body.data
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
        return locationModel
    }

    fun sendOTP(mobile: String?) {

        viewState.value = ViewState.KeyboardState()

        if (mobile.isNullOrEmpty() || mobile?.length != 11) {
            val message = "আপনার সঠিক মোবাইল নাম্বার লিখুন"
            viewState.value = ViewState.ShowMessage(message)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.sendOTP(OTPSendRequest(mobile, mobile))
            withContext(Dispatchers.Main) {
                when (response) {
                    is NetworkResponse.Success -> {
                        val message = response.body.data
                        viewState.value = ViewState.ShowMessage(message)
                        viewState.value = ViewState.NextState()
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


    private fun verifyOTP(mobile: String, code: String) {

        viewState.value = ViewState.KeyboardState()
        if (otpCode.value.isNullOrEmpty()) {
            val message = "সঠিক OTP কোড লিখুন"
            viewState.value = ViewState.ShowMessage(message)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.verifyOTP(mobile, code)
            withContext(Dispatchers.Main) {
                when (response) {
                    is NetworkResponse.Success -> {
                        val flag = response.body.data
                        if (flag == 1) {
                            val message = "OTP কোড ভেরিফাইড"
                            viewState.value = ViewState.ShowMessage(message)
                            viewState.value = ViewState.NextState()
                        } else {
                            val message = "OTP কোড সঠিক নয়"
                            viewState.value = ViewState.ShowMessage(message)
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

}