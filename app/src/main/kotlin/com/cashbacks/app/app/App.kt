package com.cashbacks.app.app

import android.app.Application
import com.cashbacks.app.di.AppComponent
import com.cashbacks.app.di.DaggerAppComponent
import com.cashbacks.app.di.modules.DataModule

class App : Application() {
    val appComponent: AppComponent by lazy {
        DaggerAppComponent.builder()
            .dataModule(DataModule(context = this))
            .build()
    }
}