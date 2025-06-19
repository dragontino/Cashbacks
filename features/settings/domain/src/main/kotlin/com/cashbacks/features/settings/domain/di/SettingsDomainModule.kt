package com.cashbacks.features.settings.domain.di

import com.cashbacks.features.settings.domain.usecase.FetchSettingsUseCase
import com.cashbacks.features.settings.domain.usecase.FetchSettingsUseCaseImpl
import com.cashbacks.features.settings.domain.usecase.GetSettingsUseCase
import com.cashbacks.features.settings.domain.usecase.GetSettingsUseCaseImpl
import com.cashbacks.features.settings.domain.usecase.UpdateSettingsUseCase
import com.cashbacks.features.settings.domain.usecase.UpdateSettingsUseCaseImpl
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val SettingsDomainModule = module {

    single<UpdateSettingsUseCase> {
        UpdateSettingsUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<FetchSettingsUseCase> {
        FetchSettingsUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<GetSettingsUseCase> {
        GetSettingsUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }
}