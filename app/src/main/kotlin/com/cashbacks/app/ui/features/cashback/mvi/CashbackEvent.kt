package com.cashbacks.app.ui.features.cashback.mvi

import com.cashbacks.app.mvi.MviEvent
import com.cashbacks.app.ui.features.bankcard.BankCardArgs
import com.cashbacks.app.ui.features.shop.ShopArgs
import com.cashbacks.app.ui.managment.DialogType

sealed class CashbackEvent : MviEvent {
    data class ShowSnackbar(val message: String) : CashbackEvent()

    data object NavigateBack : CashbackEvent()

    data class NavigateToShop(val args: ShopArgs) : CashbackEvent()

    data class NavigateToBankCard(val args: BankCardArgs) : CashbackEvent()

    data class OpenDialog(val type: DialogType) : CashbackEvent()

    data object CloseDialog : CashbackEvent()
}