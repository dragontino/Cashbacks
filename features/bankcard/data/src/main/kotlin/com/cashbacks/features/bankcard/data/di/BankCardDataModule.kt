package com.cashbacks.features.bankcard.data.di

import com.cashbacks.features.bankcard.data.repo.BankCardRepositoryImpl
import com.cashbacks.features.bankcard.domain.repo.BankCardRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val BankCardDataModule = module {
    single<BankCardRepository> {
        BankCardRepositoryImpl(
            dao = get(),
            context = androidContext()
        )
    }
}