package com.cashbacks.features.settings.presentation.di

import com.cashbacks.features.settings.presentation.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val SettingsPresentationModule = module {
    viewModelOf(::SettingsViewModel)
}