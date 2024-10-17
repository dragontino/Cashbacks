package com.cashbacks.app.ui.features.shop.mvi

import com.cashbacks.app.mvi.MviEvent
import com.cashbacks.app.ui.features.cashback.CashbackArgs
import com.cashbacks.app.ui.managment.DialogType

sealed class ShopEvent : MviEvent {
    data class ShowSnackbar(val message: String) : ShopEvent()

    data object NavigateBack : ShopEvent()

    data class NavigateToCashback(val args: CashbackArgs) : ShopEvent()

    data class ChangeOpenedDialog(val openedDialogType: DialogType?) : ShopEvent()
}