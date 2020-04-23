package com.ajkerdeal.app.essential

import android.app.Application
import com.ajkerdeal.app.essential.api.RetrofitSingleton
import com.ajkerdeal.app.essential.utils.SessionManager
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton
import timber.log.Timber

class MainApplication: Application(), KodeinAware {

    override fun onCreate() {
        super.onCreate()

        SessionManager.init(applicationContext)
        RetrofitSingleton.init(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override val kodein: Kodein = Kodein.lazy {
        import(androidXModule(this@MainApplication))
        bind() from singleton { RetrofitSingleton.instance }
        //bind() from singleton { ApiInterface(instance()) }
        //bind() from singleton { AppDatabase(instance()) }
        //bind() from singleton { FormRepository(instance(), instance()) }
        //bind() from provider { MainViewModelFactory(instance()) }
        //bind() from provider { FormListViewModelFactory(instance()) }
        //bind() from provider { AuthViewModelFactory(instance()) }
    }
}