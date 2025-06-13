package com.cashbacks.app.di.modules

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.arkivanov.mvikotlin.timetravel.store.TimeTravelStoreFactory
import com.cashbacks.app.BuildConfig
import com.cashbacks.app.R
import com.cashbacks.app.ui.MainViewModel
import com.cashbacks.common.resources.AppInfo
import com.cashbacks.common.resources.MessageHandler
import com.cashbacks.common.utils.DateTimeFormats
import com.cashbacks.common.utils.DateUtils.getDisplayableString
import com.cashbacks.common.utils.parseToDate
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val AppModule = module {
    single<StoreFactory> {
        when {
            BuildConfig.DEBUG -> LoggingStoreFactory(TimeTravelStoreFactory())
            else -> DefaultStoreFactory()
        }
    }

    single<AppInfo> {
        val version = with(androidApplication()) {
            val name = BuildConfig.VERSION_NAME
            val displayableDateString = BuildConfig.VERSION_DATE
                .parseToDate(formatBuilder = DateTimeFormats.defaultDateFormat())
                .getDisplayableString()
            getString(R.string.app_version_pattern, name, displayableDateString)
        }

        AppInfo(
            name = androidApplication().getString(R.string.activity_name),
            version = version
        )
    }

    factory {
        MessageHandler(androidApplication())
    }

    viewModelOf(::MainViewModel)
}