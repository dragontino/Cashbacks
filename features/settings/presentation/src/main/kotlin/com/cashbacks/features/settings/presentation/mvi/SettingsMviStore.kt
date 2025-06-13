package com.cashbacks.features.settings.presentation.mvi

import com.cashbacks.common.utils.management.ScreenState
import com.cashbacks.features.settings.domain.model.Settings

internal sealed interface SettingsAction {
    data object LoadingStarted : SettingsAction

    data object LoadingFinished : SettingsAction

    data class LoadSettings(val settings: Settings) : SettingsAction

    data class DisplayMessage(val message: String) : SettingsAction
}


internal sealed interface SettingsLabel {
    data object NavigateBack : SettingsLabel

    data class DisplayMessage(val message: String) : SettingsLabel
}


internal sealed interface SettingsIntent {
    data class UpdateSetting(val function: (Settings) -> Settings = { it }) : SettingsIntent

    data object ClickButtonBack : SettingsIntent
}


internal sealed interface SettingsMessage {
    data class UpdateScreenState(val screenState: ScreenState) : SettingsMessage

    data class UpdateSettings(val settings: Settings) : SettingsMessage
}


internal data class SettingsState(
    val screenState: ScreenState = ScreenState.Stable,
    val settings: Settings = Settings(),
)