package com.cashbacks.app.ui.features.home.cards.mvi

import com.cashbacks.app.mvi.MviEvent
import com.cashbacks.app.ui.features.bankcard.BankCardArgs
import com.cashbacks.app.ui.managment.DialogType

sealed class CardsEvent : MviEvent {
    data class ShowSnackbar(val message: String) : CardsEvent()

    data class NavigateToBankCard(val args: BankCardArgs) : CardsEvent()

    data object OpenNavigationDrawer : CardsEvent()

    data class ChangeOpenedDialog(val openedDialogType: DialogType?) : CardsEvent()
}