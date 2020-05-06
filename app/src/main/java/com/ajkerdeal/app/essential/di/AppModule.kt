package com.ajkerdeal.app.essential.di

import com.ajkerdeal.app.essential.api.ApiInterface
import com.ajkerdeal.app.essential.api.RetrofitSingleton
import com.ajkerdeal.app.essential.repository.AppRepository
import com.ajkerdeal.app.essential.ui.auth.AuthViewModel
import com.ajkerdeal.app.essential.ui.home.HomeViewModel
import com.ajkerdeal.app.essential.ui.home.parcel.ParcelViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { RetrofitSingleton.instance }
    single { ApiInterface(get()) }
    single { AppRepository(get()) }
    viewModel { AuthViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { ParcelViewModel(get()) }
}