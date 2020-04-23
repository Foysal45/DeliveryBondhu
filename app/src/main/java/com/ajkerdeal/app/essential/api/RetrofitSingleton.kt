package com.ajkerdeal.app.essential.api

import android.app.Application
import com.ajkerdeal.app.essential.BuildConfig
import com.ajkerdeal.app.essential.utils.AppConstant
import com.google.gson.GsonBuilder
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object RetrofitSingleton {


    private lateinit var application: Application
    private const val cacheSize = 5L * 1024L * 1024L // 5 MB

    fun init(application: Application) {
        this.application = application
    }

    private val gson by lazy {
        GsonBuilder().setLenient().setPrettyPrinting().create()
    }

    private val cache by lazy {
        Cache(File(application.cacheDir, "${application.packageName}.cache"), cacheSize)
    }

    private val httpClient by lazy {
        OkHttpClient.Builder().apply {
            if (BuildConfig.DEBUG) {
                val httpLoggingInterceptor = HttpLoggingInterceptor()
                val logging =
                    httpLoggingInterceptor.apply {
                        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
                    }
                addInterceptor(logging)
            }
            //addInterceptor(NetworkConnectionInterceptor(application))
            //addInterceptor(AuthInterceptor())
            //addNetworkInterceptor(NetworkInterceptor())
            //addInterceptor(OfflineInterceptor(application))
            cache(cache)
            readTimeout(30, TimeUnit.SECONDS)
            writeTimeout(1, TimeUnit.MINUTES)
            connectTimeout(30, TimeUnit.SECONDS)

        }.build()
    }

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .addCallAdapterFactory(NetworkResponseAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(AppConstant.BASE_URL)
            .client(httpClient)
            .build()
    }

}