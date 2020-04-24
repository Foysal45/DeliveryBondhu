package com.ajkerdeal.app.essential

import android.app.Application
import com.ajkerdeal.app.essential.api.RetrofitSingleton
import com.ajkerdeal.app.essential.di.appModule
import com.ajkerdeal.app.essential.utils.SessionManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        SessionManager.init(applicationContext)
        RetrofitSingleton.init(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidContext(this@MainApplication)
            modules(listOf(appModule))
        }
    }

    //override val kodein: Kodein = Kodein.lazy {
        //import(androidXModule(this@MainApplication))
        //bind() from singleton { RetrofitSingleton.instance }
        //bind() from singleton { ApiInterface(instance()) }
        //bind() from singleton { AppDatabase(instance()) }
        //bind() from singleton { FormRepository(instance(), instance()) }
        //bind() from provider { MainViewModelFactory(instance()) }
        //bind() from provider { FormListViewModelFactory(instance()) }
        //bind() from provider { AuthViewModelFactory(instance()) }
    //}
}