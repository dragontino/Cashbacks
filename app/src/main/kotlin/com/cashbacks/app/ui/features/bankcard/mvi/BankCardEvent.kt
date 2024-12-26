package com.cashbacks.app.ui.features.bankcard.mvi

import androidx.compose.ui.text.AnnotatedString
import com.cashbacks.app.mvi.MviEvent
import com.cashbacks.app.ui.features.bankcard.BankCardArgs
import com.cashbacks.app.ui.managment.BottomSheetType
import com.cashbacks.app.ui.managment.DialogType

sealed class BankCardViewingEvent : MviEvent {
    data class ShowSnackbar(val message: String) : BankCardViewingEvent()

    data class CopyText(val text: AnnotatedString) : BankCardViewingEvent()

    data class NavigateToEditingBankCard(val args: BankCardArgs) : BankCardViewingEvent()

    data class ChangeOpenedDialog(val openedDialogType: DialogType?) : BankCardViewingEvent()

    data object NavigateBack : BankCardViewingEvent()
}


sealed class BankCardEditingEvent : MviEvent {
    data class ShowSnackbar(val message: String) : BankCardEditingEvent()

    data class ChangeOpenedDialog(val openedDialogType: DialogType?) : BankCardEditingEvent()

    data class OpenBottomSheet(val type: BottomSheetType) : BankCardEditingEvent()

    data object CloseBottomSheet : BankCardEditingEvent()

    data object NavigateBack : BankCardEditingEvent()
}