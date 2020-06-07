package com.ajkerdeal.app.essential.di

import com.ajkerdeal.app.essential.api.ApiInterface
import com.ajkerdeal.app.essential.api.RetrofitSingleton
import com.ajkerdeal.app.essential.repository.AppRepository
import com.ajkerdeal.app.essential.ui.auth.AuthViewModel
import com.ajkerdeal.app.essential.ui.home.HomeActivityViewModel
import com.ajkerdeal.app.essential.ui.home.HomeViewModel
import com.ajkerdeal.app.essential.ui.home.dashboard.DashboardViewModel
import com.ajkerdeal.app.essential.ui.home.parcel.ParcelViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { RetrofitSingleton.instance }
    single { ApiInterface(get()) }
    single { AppRepository(get()) }
    single { AuthViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { ParcelViewModel(get()) }
    viewModel { HomeActivityViewModel(get()) }
    viewModel { DashboardViewModel(get()) }
}