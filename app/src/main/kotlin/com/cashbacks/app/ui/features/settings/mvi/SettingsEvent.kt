package com.cashbacks.app.ui.features.settings.mvi

import com.cashbacks.app.mvi.MviEvent

sealed class SettingsEvent : MviEvent {
    data class ShowSnackbar(val message: String) : SettingsEvent()

    data object NavigateBack : SettingsEvent()
}