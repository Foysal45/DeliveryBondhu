package com.ajkerdeal.app.essential.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajkerdeal.app.essential.api.models.auth.fcm.UpdateTokenRequest
import com.ajkerdeal.app.essential.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeActivityViewModel(private val repository: AppRepository): ViewModel() {

    fun clearFirebaseToken(userId: Int) {

        viewModelScope.launch (Dispatchers.IO) {
            repository.updateFirebaseToken(UpdateTokenRequest(userId, ""))
        }
    }
}