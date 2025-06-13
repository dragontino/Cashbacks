package com.cashbacks.features.shop.data.di

import com.cashbacks.features.shop.data.repo.ShopRepositoryImpl
import com.cashbacks.features.shop.domain.repo.ShopRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val ShopDataModule = module {
    single<ShopRepository> {
        ShopRepositoryImpl(
            dao = get(),
            context = androidContext()
        )
    }
}