package com.ajkerdeal.app.essential.ui.chat.compose

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajkerdeal.app.essential.api.models.fcm.FCMRequest
import com.ajkerdeal.app.essential.api.models.fcm.FCMResponse
import com.ajkerdeal.app.essential.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatComposeViewModel(private val repository: AppRepository): ViewModel() {

    fun sendPushNotifications(authToken: String, requestBody: FCMRequest): LiveData<FCMResponse> {
        val responseData = MutableLiveData<FCMResponse>()
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.sendPushNotifications(authToken, requestBody)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        responseData.value = response.body()
                    }
                }
            }
        }
        return responseData
    }

}