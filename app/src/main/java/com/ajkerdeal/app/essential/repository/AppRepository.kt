package com.ajkerdeal.app.essential.repository

import com.ajkerdeal.app.essential.api.ApiInterface
import com.ajkerdeal.app.essential.api.models.auth.LoginRequest

class AppRepository(private val apiInterface: ApiInterface) {

    suspend fun authUser(body: LoginRequest) = apiInterface.login(body)

}