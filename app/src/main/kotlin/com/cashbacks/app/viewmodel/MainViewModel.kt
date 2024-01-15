package com.cashbacks.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashbacks.domain.model.Settings
import com.cashbacks.domain.usecase.SettingsUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainViewModel(settingsUseCase: SettingsUseCase) : ViewModel() {

    var settings by mutableStateOf(Settings())
        private set

    init {
        settingsUseCase.fetchSettings()
            .onEach { settings = it }
            .launchIn(viewModelScope)
    }
}