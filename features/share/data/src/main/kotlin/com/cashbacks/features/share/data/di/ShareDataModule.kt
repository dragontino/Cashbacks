package com.cashbacks.features.share.data.di

import com.cashbacks.core.database.AppDatabase
import com.cashbacks.features.share.data.repo.ShareDataRepositoryImpl
import com.cashbacks.features.share.domain.repo.ShareDataRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val ShareDataModule = module {
    single<ShareDataRepository> {
        ShareDataRepositoryImpl(
            database = get<AppDatabase>(),
            context = androidContext()
        )
    }
}