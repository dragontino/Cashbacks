package com.cashbacks.app.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashbacks.domain.model.Settings
import com.cashbacks.domain.usecase.settings.SettingsUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainViewModel(settingsUseCase: SettingsUseCase) : ViewModel() {

    private val _settings = mutableStateOf(Settings())
    val settings = derivedStateOf { _settings.value }

    init {
        settingsUseCase.fetchSettings()
            .onEach { _settings.value = it }
            .launchIn(viewModelScope)
    }
}