package com.cashbacks.app.app

import android.app.Application
import com.cashbacks.app.BuildConfig
import com.cashbacks.app.R
import com.cashbacks.app.di.AppComponent
import com.cashbacks.app.di.DaggerAppComponent
import com.cashbacks.app.di.modules.AppModule
import com.cashbacks.app.di.modules.DataModule
import com.cashbacks.domain.util.parseToDate
import com.cashbacks.domain.util.parseToString

class App : Application() {

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.builder()
            .dataModule(DataModule(context = this))
            .appModule(AppModule(application = this))
            .build()
    }

    val version by lazy {
        val name = BuildConfig.VERSION_NAME
        val date = BuildConfig.VERSION_DATE.parseToDate("dd/MM/yyyy")
        val formattedDateString = date.parseToString("dd MMMM yyyy")
        return@lazy getString(R.string.app_version_pattern, name, formattedDateString)
    }

    val name by lazy { getString(R.string.app_name) }

    @Suppress("UNUSED_PARAMETER")
    var needToDeleteExpiredCashbacks: Boolean = true
        set(value) {
            field = false
        }
}