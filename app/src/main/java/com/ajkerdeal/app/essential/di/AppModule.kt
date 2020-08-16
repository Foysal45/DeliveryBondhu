package com.ajkerdeal.app.essential.di

import com.ajkerdeal.app.essential.api.ApiInterfaceANA
import com.ajkerdeal.app.essential.api.ApiInterfaceAPI
import com.ajkerdeal.app.essential.api.RetrofitUtils.createCache
import com.ajkerdeal.app.essential.api.RetrofitUtils.createOkHttpClient
import com.ajkerdeal.app.essential.api.RetrofitUtils.createOkHttpClientFile
import com.ajkerdeal.app.essential.api.RetrofitUtils.getGson
import com.ajkerdeal.app.essential.api.RetrofitUtils.retrofitInstance
import com.ajkerdeal.app.essential.repository.AppRepository
import com.ajkerdeal.app.essential.ui.auth.AuthViewModel
import com.ajkerdeal.app.essential.ui.home.HomeActivityViewModel
import com.ajkerdeal.app.essential.ui.home.HomeViewModel
import com.ajkerdeal.app.essential.ui.home.dashboard.DashboardViewModel
import com.ajkerdeal.app.essential.ui.home.parcel.ParcelViewModel
import com.ajkerdeal.app.essential.ui.profile.ProfileViewModel
import com.ajkerdeal.app.essential.utils.AppConstant
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {

    single { getGson() }
    single { createCache(get()) }
    single { createOkHttpClient(get()) }
    single(named("clientUpload")) { createOkHttpClientFile(get()) }

    single(named("api")) { retrofitInstance(AppConstant.BASE_URL_API, get(), get(named("clientUpload"))) }
    single(named("ana")) { retrofitInstance(AppConstant.BASE_URL_ANA, get(), get(named("clientUpload"))) }
    single { ApiInterfaceAPI(get(named("api"))) }
    single { ApiInterfaceANA(get(named("ana"))) }

    single { AppRepository(get(), get()) }

    single { AuthViewModel(get()) }
    single { HomeActivityViewModel(get()) }
    viewModel { DashboardViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { ParcelViewModel(get()) }

}