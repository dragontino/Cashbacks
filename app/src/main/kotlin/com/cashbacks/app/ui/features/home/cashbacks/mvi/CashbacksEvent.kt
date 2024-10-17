package com.cashbacks.app.ui.features.home.cashbacks.mvi

import com.cashbacks.app.mvi.MviEvent
import com.cashbacks.app.ui.features.cashback.CashbackArgs
import com.cashbacks.app.ui.managment.DialogType

sealed class CashbacksEvent : MviEvent {
    data object NavigateBack : CashbacksEvent()

    data class ShowSnackbar(val message: String) : CashbacksEvent()

    data class OpenDialog(val type: DialogType) : CashbacksEvent()

    data object CloseDialog : CashbacksEvent()

    data class NavigateToCashback(val args: CashbackArgs) : CashbacksEvent()

    data object OpenNavigationDrawer : CashbacksEvent()
}