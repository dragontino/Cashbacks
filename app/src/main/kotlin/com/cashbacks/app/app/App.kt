package com.cashbacks.app.app

import android.app.Application
import com.cashbacks.app.BuildConfig
import com.cashbacks.app.di.AppComponent
import com.cashbacks.app.di.DaggerAppComponent
import com.cashbacks.app.di.modules.AppModule
import com.cashbacks.app.di.modules.DataModule
import com.cashbacks.domain.R
import com.cashbacks.domain.util.DateTimeFormats
import com.cashbacks.domain.util.getDisplayableDateString

class App : Application() {

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.builder()
            .dataModule(DataModule(context = this))
            .appModule(AppModule(application = this))
            .build()
    }

    val version by lazy {
        val name = BuildConfig.VERSION_NAME
        val displayableDateString = getDisplayableDateString(
            dateString = BuildConfig.VERSION_DATE,
            inputFormatBuilder = DateTimeFormats.defaultDateFormat()
        )
        return@lazy getString(R.string.app_version_pattern, name, displayableDateString)
    }

    val name by lazy { getString(R.string.app_name) }

    @Suppress("UNUSED_PARAMETER")
    var needToDeleteExpiredCashbacks: Boolean = true
        set(value) {
            field = false
        }
}