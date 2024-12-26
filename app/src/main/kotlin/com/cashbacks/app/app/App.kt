package com.cashbacks.app.app

import android.app.Application
import com.cashbacks.app.BuildConfig
import com.cashbacks.app.di.AppComponent
import com.cashbacks.app.di.DaggerAppComponent
import com.cashbacks.app.di.modules.AppModule
import com.cashbacks.app.di.modules.DataModule
import com.cashbacks.app.util.DateUtils.getDisplayableString
import com.cashbacks.domain.R
import com.cashbacks.domain.util.DateTimeFormats
import com.cashbacks.domain.util.parseToDate

class App : Application() {

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.builder()
            .dataModule(DataModule(context = this))
            .appModule(AppModule(application = this))
            .build()
    }

    val version by lazy {
        val name = BuildConfig.VERSION_NAME
        val displayableDateString = BuildConfig.VERSION_DATE
            .parseToDate(formatBuilder = DateTimeFormats.defaultDateFormat())
            .getDisplayableString()
        return@lazy getString(R.string.app_version_pattern, name, displayableDateString)
    }

    val name by lazy { getString(R.string.activity_name) }

    var checkExpiredCashbacks: Boolean = true
        set(value) {
            field = false
        }
}