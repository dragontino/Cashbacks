package com.cashbacks.app.di.modules

import com.cashbacks.domain.repository.SettingsRepository
import com.cashbacks.domain.usecase.settings.SettingsUseCase
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers

@Module
class SettingsModule {
    @Provides
    fun provideSettingsUseCase(settingsRepository: SettingsRepository) = SettingsUseCase(
        repository = settingsRepository,
        dispatcher = Dispatchers.IO
    )
}