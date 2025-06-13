package com.cashbacks.features.cashback.data.di

import com.cashbacks.features.cashback.data.repo.CashbackRepositoryImpl
import com.cashbacks.features.cashback.domain.repo.CashbackRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val CashbackDataModule = module {
    single<CashbackRepository> {
        CashbackRepositoryImpl(
            dao = get(),
            context = androidContext()
        )
    }
}