package com.ajkerdeal.app.essential.di

import com.ajkerdeal.app.essential.api.*
import com.ajkerdeal.app.essential.api.RetrofitUtils.createCache
import com.ajkerdeal.app.essential.api.RetrofitUtils.createOkHttpClient
import com.ajkerdeal.app.essential.api.RetrofitUtils.createOkHttpClientFile
import com.ajkerdeal.app.essential.api.RetrofitUtils.getGson
import com.ajkerdeal.app.essential.api.RetrofitUtils.retrofitInstance
import com.ajkerdeal.app.essential.database.AppDatabase
import com.ajkerdeal.app.essential.repository.AppRepository
import com.ajkerdeal.app.essential.ui.auth.AuthViewModel
import com.ajkerdeal.app.essential.ui.chat.compose.ChatComposeViewModel
import com.ajkerdeal.app.essential.ui.home.HomeActivityViewModel
import com.ajkerdeal.app.essential.ui.home.HomeViewModel
import com.ajkerdeal.app.essential.ui.home.dashboard.DashboardViewModel
import com.ajkerdeal.app.essential.ui.home.parcel.ParcelViewModel
import com.ajkerdeal.app.essential.ui.home.weight_selection.WeightSelectionViewModel
import com.ajkerdeal.app.essential.ui.profile.ProfileViewModel
import com.ajkerdeal.app.essential.ui.quick_order_scan.QuickOrderViewModel
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
    single(named("adm")) { retrofitInstance(AppConstant.BASE_URL_ADM, get(), get(named("clientUpload"))) }
    single(named("adcore")) { retrofitInstance(AppConstant.BASE_URL_ADCORE, get(), get(named("clientUpload"))) }
    single(named("fcm")) { retrofitInstance(AppConstant.BASE_URL_FCM, get(), get(named("clientUpload"))) }
    single { ApiInterfaceAPI(get(named("api"))) }
    single { ApiInterfaceANA(get(named("ana"))) }
    single { ApiInterfaceADM(get(named("adm"))) }
    single { ApiInterfaceADCORE(get(named("adcore"))) }
    single { ApiInterfaceFCM(get(named("fcm"))) }

    single { AppDatabase.invoke(get()) }

    single { AppRepository(get(), get(), get(), get(), get(), get()) }

    single { AuthViewModel(get()) }
    single { HomeActivityViewModel(get()) }
    viewModel { DashboardViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { ParcelViewModel(get()) }
    viewModel { WeightSelectionViewModel(get()) }
    viewModel { QuickOrderViewModel(get()) }
    viewModel { ChatComposeViewModel(get()) }

}