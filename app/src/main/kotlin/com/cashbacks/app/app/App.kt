package com.cashbacks.app.app

import android.app.Application
import com.cashbacks.app.di.ApplicationModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {

    var checkExpiredCashbacks: Boolean = true
        set(value) {
            field = false
        }


    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            androidLogger()

            modules(ApplicationModules)
        }
    }
}