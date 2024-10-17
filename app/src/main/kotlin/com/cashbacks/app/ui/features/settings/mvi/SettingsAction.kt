package com.cashbacks.app.ui.features.settings.mvi

import com.cashbacks.app.mvi.MviAction
import com.cashbacks.domain.model.Settings

sealed class SettingsAction : MviAction {
    data class UpdateSetting(val function: (Settings) -> Settings = { it }) : SettingsAction()

    data object ClickButtonBack : SettingsAction()
}