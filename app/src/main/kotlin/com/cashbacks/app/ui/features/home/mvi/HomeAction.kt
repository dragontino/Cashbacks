package com.cashbacks.app.ui.features.home.mvi

import com.cashbacks.app.mvi.MviAction

sealed class HomeAction : MviAction {
    data class ClickButtonExportData(val onSuccess: (path: String) -> Unit = {}) : HomeAction()

    data object ClickButtonOpenSettings : HomeAction()

    data class ShowMessage(val message: String) : HomeAction()

    data object ClickButtonOpenDrawer : HomeAction()

    data object ClickButtonCloseDrawer : HomeAction()
}
