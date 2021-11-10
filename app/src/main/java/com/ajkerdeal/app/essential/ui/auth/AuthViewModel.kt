package com.ajkerdeal.app.essential.ui.auth

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajkerdeal.app.essential.api.models.auth.LoginRequest
import com.ajkerdeal.app.essential.api.models.auth.otp.OTPSendRequest
import com.ajkerdeal.app.essential.api.models.auth.reset_password.CheckMobileRequest
import com.ajkerdeal.app.essential.api.models.auth.reset_password.UpdatePasswordRequest
import com.ajkerdeal.app.essential.api.models.auth.reset_password.UpdatePasswordRequestDT
import com.ajkerdeal.app.essential.api.models.auth.signup.SignUpRequest
import com.ajkerdeal.app.essential.api.models.features.FeatureData
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

    // Login
    val userId = MutableLiveData<String>("")
    val password = MutableLiveData<String>("")
    // SignUp
    val name = MutableLiveData<String>("")
    val userId1 = MutableLiveData<String>("")
    val alterPhoneNumber = MutableLiveData<String>("")
    val bKashAccountNumber = MutableLiveData<String>("")
    val password1 = MutableLiveData<String>("")
    val confirmPassword = MutableLiveData<String>("")
    val address = MutableLiveData<String>("")
    val districtId = MutableLiveData<Int>(0)
    val thanaId = MutableLiveData<Int>(0)
    val postCode = MutableLiveData<Int>(0)
    val districtName = MutableLiveData<String>("")
    val thanaName = MutableLiveData<String>("")

    var otpMobile: String? = ""
    var otpType: Int = 0

    // Password Reset
    val resetMobile = MutableLiveData<String>("")
    val otpCode = MutableLiveData<String>("")
    val firebaseToken = MutableLiveData<String>("")
    val appVersion = MutableLiveData<String>("")
    var deliveryUserId: Int = 0

    // Reset Password Form
    val newPassword = MutableLiveData<String>("")
    val newConfirmPassword = MutableLiveData<String>("")

    val progress = MutableLiveData<Boolean>()
    val enableBtn = MutableLiveData<Boolean>()
    val viewState = MutableLiveData<ViewState>(ViewState.NONE)


    fun onLoginClicked(view: View) {
        authUser()
    }

    fun onRegistrationClicked(view: View) {
        signUpUser()
    }

    fun onResetPassword(view: View) {
        val mobile = resetMobile.value ?: ""
        checkMobile(mobile)
    }

    fun onResetPasswordForm(view: View) {
        updatePassword()
    }

    fun onOTPSubmit(view: View) {
        verifyOTP(otpMobile ?: "", otpCode.value ?: "")
    }

    private fun authUser() {

        viewState.value = ViewState.KeyboardState()
        if (!validate()) {
            return
        }

        progress.value = true
        viewModelScope.launch(Dispatchers.IO) {

            val requestBody = LoginRequest(userId.value?.trim(), password.value?.trim(), firebaseToken.value)
            val response = repository.authUser(requestBody)
            val response1 = repository.dtLogin(requestBody)
            withContext(Dispatchers.Main) {
                progress.value = false
                when (response) {
                    is NetworkResponse.Success -> {
                        val data = response.body.model
                        if (data != null && data.deliveryUserId != 0) {

                            SessionManager.createSession(data.deliveryUserId,data.deliveryUserName,data.mobileNumber,data.bkashMobileNumber)
                            SessionManager.userPic = data.profileImage ?: ""
                            userId.value = ""
                            password.value = ""
                        } else {
                            viewState.setValue(ViewState.ShowMessage(data?.message))
                        }
                    }
                    is NetworkResponse.ServerError -> {
                        val message = "দুঃখিত, এই মুহূর্তে আমাদের সার্ভার কানেকশনে সমস্যা হচ্ছে, কিছুক্ষণ পর আবার চেষ্টা করুন"
                        viewState.setValue(ViewState.ShowMessage(message))

                    }
                    is NetworkResponse.NetworkError -> {
                        val message = "দুঃখিত, এই মুহূর্তে আপনার ইন্টারনেট কানেকশনে সমস্যা হচ্ছে"
                        viewState.setValue(ViewState.ShowMessage(message))
                    }
                    is NetworkResponse.UnknownError -> {
                        val message = "কোথাও কোনো সমস্যা হচ্ছে, আবার চেষ্টা করুন"
                        viewState.value = ViewState.ShowMessage(message)
                        Timber.d(response.error)
                    }
                }.exhaustive

                when (response1) {
                    is NetworkResponse.Success -> {
                        val data = response1.body.model
                        if (data != null && data.deliveryUserId != 0) {
                            SessionManager.dtUserId = data.deliveryUserId
                            viewState.value = ViewState.NextState()
                        } else {
                            viewState.setValue(ViewState.ShowMessage(data?.message))
                        }
                    }
                    is NetworkResponse.ServerError -> {
                        val message = "দুঃখিত, এই মুহূর্তে আমাদের সার্ভার কানেকশনে সমস্যা হচ্ছে, কিছুক্ষণ পর আবার চেষ্টা করুন"
                        viewState.setValue(ViewState.ShowMessage(message))

                    }
                    is NetworkResponse.NetworkError -> {
                        val message = "দুঃখিত, এই মুহূর্তে আপনার ইন্টারনেট কানেকশনে সমস্যা হচ্ছে"
                        viewState.setValue(ViewState.ShowMessage(message))
                    }
                    is NetworkResponse.UnknownError -> {
                        val message = "কোথাও কোনো সমস্যা হচ্ছে, আবার চেষ্টা করুন"
                        viewState.value = ViewState.ShowMessage(message)
                        Timber.d(response1.error)
                    }
                }.exhaustive

                viewState.value = ViewState.NONE
            }

        }

    }

    private fun validate(): Boolean {

        if (userId.value?.trim().isNullOrEmpty() || userId.value?.length != 11) {
            val message = "আপনার সঠিক মোবাইল নম্বর লিখুন"
            viewState.value = ViewState.ShowMessage(message)
            viewState.value = ViewState.NONE
            return false
        }

        if (password.value?.trim().isNullOrEmpty()) {
            val message = "আপনার পাসওয়ার্ড লিখুন"
            viewState.value = ViewState.ShowMessage(message)
            viewState.value = ViewState.NONE
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

            val requestBody = SignUpRequest(name.value?.trim(), userId1.value?.trim(),
                alterPhoneNumber.value?.trim(), bKashAccountNumber.value?.trim() ,
                password1.value?.trim(), address.value,
                districtId.value ?: 0,
                thanaId.value ?: 0,
                postCode.value ?: 0,
                districtName.value, thanaName.value,
                appVersion.value
            )
            val responseDT = repository.deliveryManRegistration(requestBody)
            if (responseDT is NetworkResponse.Success) {
                requestBody.dtId = responseDT.body.model?.customerId ?: 0
            }
            val response = repository.signUpUser(requestBody)
            withContext(Dispatchers.Main) {
                progress.value = false
                when (response) {
                    is NetworkResponse.Success -> {

                        val data = response.body.data

                        if (data != null && data.customerId != 0) {
                            viewState.value = ViewState.ShowMessage(data.message)

                            //otpType = 2
                            //sendOTP(userId1.value ?: "")
                            password1.value = ""
                            confirmPassword.value = ""
                            viewState.value = ViewState.NextState()
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
                viewState.value = ViewState.NONE
            }
        }

    }

    private fun validateSignUp(): Boolean {

        if (name.value?.trim().isNullOrEmpty()) {
            val message = "আপনার নাম লিখুন"
            viewState.value = ViewState.ShowMessage(message)
            viewState.value = ViewState.NONE
            return false
        }

        if (userId1.value?.trim().isNullOrEmpty() || userId1.value?.length != 11) {
            val message = "আপনার সঠিক মোবাইল নম্বর লিখুন"
            viewState.value = ViewState.ShowMessage(message)
            viewState.value = ViewState.NONE
            return false
        }

        if (bKashAccountNumber.value?.trim().isNullOrEmpty() || bKashAccountNumber.value?.length != 11) {
            val message = "আপনার সঠিক বিকাশ একাউন্ট নম্বর লিখুন"
            viewState.value = ViewState.ShowMessage(message)
            viewState.value = ViewState.NONE
            return false
        }

        if (password1.value?.trim().isNullOrEmpty()) {
            val message = "আপনার পাসওয়ার্ড লিখুন"
            viewState.value = ViewState.ShowMessage(message)
            viewState.value = ViewState.NONE
            return false
        }

        if (confirmPassword.value?.trim().isNullOrEmpty()) {
            val message = "আপনার কনফার্ম পাসওয়ার্ড লিখুন"
            viewState.value = ViewState.ShowMessage(message)
            viewState.value = ViewState.NONE
            return false
        }

        if (password1.value != confirmPassword.value) {
            val message = "পাসওয়ার্ড দুটি মিলছে না"
            viewState.value = ViewState.ShowMessage(message)
            viewState.value = ViewState.NONE
            return false
        }

        if (districtId.value == 0) {
            val message = "বর্তমান কর্মস্থান নির্বাচন করুন"
            viewState.value = ViewState.ShowMessage(message)
            viewState.value = ViewState.NONE
            return false
        }

        if (thanaId.value == 0) {
            val message = "এরিয়া নির্বাচন করুন"
            viewState.value = ViewState.ShowMessage(message)
            viewState.value = ViewState.NONE
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
                        locationModel.value = response.body.data!!
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
                viewState.value = ViewState.NONE
            }
        }
        return locationModel
    }

    private fun checkMobile(mobile: String?) {

        viewState.value = ViewState.KeyboardState()

        if (mobile?.trim().isNullOrEmpty() || mobile?.length != 11) {
            val message = "আপনার সঠিক মোবাইল নম্বর লিখুন"
            viewState.value = ViewState.ShowMessage(message)

            viewState.value = ViewState.NONE
            return
        }
        progress.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.checkMobileNumber(CheckMobileRequest(mobile))
            withContext(Dispatchers.Main) {
                progress.value = false
                when (response) {
                    is NetworkResponse.Success -> {

                            if (response.body.data?.deliveryUserId == 0) {
                                deliveryUserId = 0
                                val message = "এই নম্বরটি দিয়ে রেজিস্ট্রেশন করা হয়নি"
                                if (otpType == 2) { //Reg
                                    sendOTP(mobile)
                                } else {
                                    viewState.value = ViewState.ShowMessage(message)
                                }

                            } else {
                                deliveryUserId = response.body.data?.deliveryUserId ?: 0
                                val message = "এই নম্বরটি দিয়ে ইতিমধ্যে রেজিস্ট্রেশন করা হয়েছে"
                                //viewState.value = ViewState.NextState()
                                if (otpType == 1) {
                                    sendOTP(mobile)
                                } else {
                                    viewState.value = ViewState.ShowMessage(message)
                                }
                            }
                        //Timber.d("checkMobile $response")

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

                viewState.value = ViewState.NONE
            }
        }

    }

    private fun sendOTP(mobile: String?) {

        viewState.value = ViewState.KeyboardState()

        if (mobile.isNullOrEmpty() || mobile?.length != 11) {
            val message = "আপনার সঠিক মোবাইল নাম্বার লিখুন"
            viewState.value = ViewState.ShowMessage(message)

            viewState.value = ViewState.NONE
            return
        }

        otpMobile = mobile

        progress.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.sendOTP(OTPSendRequest(mobile, mobile))
            withContext(Dispatchers.Main) {
                progress.value = false
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
                viewState.value = ViewState.NONE
            }
        }
    }

    private fun verifyOTP(mobile: String, code: String) {

        viewState.value = ViewState.KeyboardState()
        if (otpCode.value?.trim().isNullOrEmpty()) {
            val message = "সঠিক OTP কোড লিখুন"
            viewState.value = ViewState.ShowMessage(message)
            viewState.value = ViewState.NONE
            return
        }
        progress.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.verifyOTP(mobile, code)
            withContext(Dispatchers.Main) {
                progress.value = false
                when (response) {
                    is NetworkResponse.Success -> {

                        val flag = response.body.data
                        if (flag == 1) {
                            val message = "OTP কোড ভেরিফাইড"
                            viewState.value = ViewState.ShowMessage(message)
                            if (otpType == 2) {
                                userId1.value = resetMobile.value
                            }
                            viewState.value = ViewState.NextState(otpType)
                            otpCode.value = ""
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
                viewState.value = ViewState.NONE
            }
        }
    }

    private fun updatePassword() {

        viewState.value = ViewState.KeyboardState()
        if (newPassword.value?.trim().isNullOrEmpty()) {
            val message = "আপনার পাসওয়ার্ড লিখুন"
            viewState.value = ViewState.ShowMessage(message)
            viewState.value = ViewState.NONE
            return
        }

        if (newConfirmPassword.value?.trim().isNullOrEmpty()) {
            val message = "আপনার কনফার্ম পাসওয়ার্ড লিখুন"
            viewState.value = ViewState.ShowMessage(message)
            viewState.value = ViewState.NONE
            return
        }

        if (newPassword.value != newConfirmPassword.value) {
            val message = "পাসওয়ার্ড দুটি মিলছে না"
            viewState.value = ViewState.ShowMessage(message)
            viewState.value = ViewState.NONE
            return
        }

        progress.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.updatePasswordDT(UpdatePasswordRequestDT(newPassword.value, deliveryUserId, resetMobile.value))
            withContext(Dispatchers.Main) {
                progress.value = false
                when (response) {
                    is NetworkResponse.Success -> {
                        val model = response.body.model
                        if (model != null && model > 0) {
                            updatePasswordAD()
                        } else {
                            val message = "কোথাও কোনো সমস্যা হচ্ছে, আবার চেষ্টা করুন"
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
                viewState.value = ViewState.NONE
            }
        }

    }

    private fun updatePasswordAD(){
        progress.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.updatePassword(UpdatePasswordRequest(newPassword.value, deliveryUserId, resetMobile.value))
            withContext(Dispatchers.Main) {
                progress.value = false
                when (response) {
                    is NetworkResponse.Success -> {
                        val data = response.body.data
                        if (data != null && data.customerId != 0) {
                            viewState.value = ViewState.ShowMessage(data.message)

                            newPassword.value = ""
                            newConfirmPassword.value = ""
                            viewState.value = ViewState.NextState()
                        } else {
                            viewState.value = ViewState.ShowMessage(data?.message)
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
                viewState.value = ViewState.NONE
            }
        }
    }

    fun clearLogin() {
        userId.value = ""
        password.value = ""
    }

    fun clearSignUp() {
        name.value = ""
        userId1.value = ""
        password1.value = ""
        confirmPassword.value = ""
        districtId.value = 0
        thanaId.value = 0
        postCode.value = 0
        address.value = ""
        alterPhoneNumber.value = ""
        bKashAccountNumber.value = ""
    }

    fun clearResetPasswordForm() {
        newPassword.value = ""
        newConfirmPassword.value = ""
    }

    fun features(): LiveData<FeatureData> {

        val responseBody = MutableLiveData<FeatureData>()
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.features()
            withContext(Dispatchers.Main) {

                when (response) {
                    is NetworkResponse.Success -> {
                        responseBody.value = response.body.data!!
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