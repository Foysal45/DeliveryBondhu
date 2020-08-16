package com.ajkerdeal.app.essential

import android.app.Application
import com.ajkerdeal.app.essential.di.appModule
import com.ajkerdeal.app.essential.utils.SessionManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        SessionManager.init(applicationContext)
        Timber.plant(Timber.DebugTree())

        startKoin {
            androidContext(this@MainApplication)
            modules(listOf(appModule))
        }
    }
}