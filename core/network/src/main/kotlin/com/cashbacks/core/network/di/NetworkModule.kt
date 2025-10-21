package com.cashbacks.core.network.di

import com.cashbacks.core.network.NetworkService
import com.cashbacks.core.network.NetworkServiceImpl
import com.cashbacks.core.network.retrofit.AppVersionService
import com.cashbacks.core.network.retrofit.buildAppRepositoryRetrofit
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module
import retrofit2.Retrofit

val NetworkModule = module {
    single<AppVersionService> {
        get<Retrofit>(qualifier("AppRepository")).create(AppVersionService::class.java)
    }

    single<Retrofit>(qualifier("AppRepository")) {
        buildAppRepositoryRetrofit()
    }

    single<NetworkService> {
        NetworkServiceImpl(
            appVersionService = get(),
            dispatcher = Dispatchers.IO
        )
    }
}