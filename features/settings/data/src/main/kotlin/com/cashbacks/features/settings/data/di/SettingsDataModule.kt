package com.cashbacks.features.settings.data.di

import com.cashbacks.features.settings.data.repo.SettingsRepositoryImpl
import com.cashbacks.features.settings.domain.repo.SettingsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val SettingsDataModule = module {
    single<SettingsRepository> {
        SettingsRepositoryImpl(
            dao = get(),
            context = androidContext()
        )
    }
}