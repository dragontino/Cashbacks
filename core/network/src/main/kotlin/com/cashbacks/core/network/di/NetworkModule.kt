package com.cashbacks.core.network.di

import com.cashbacks.core.network.NetworkService
import com.cashbacks.core.network.NetworkServiceImpl
import com.cashbacks.core.network.retrofit.AppVersionService
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val NetworkModule = module {
    single<AppVersionService> {
        AppVersionService()
    }

    single<NetworkService> {
        NetworkServiceImpl(
            appVersionService = get(),
            dispatcher = Dispatchers.IO
        )
    }
}