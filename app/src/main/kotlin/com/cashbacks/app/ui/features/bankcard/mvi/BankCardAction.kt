package com.cashbacks.app.ui.features.bankcard.mvi

import androidx.compose.ui.text.AnnotatedString
import com.cashbacks.app.mvi.MviAction
import com.cashbacks.app.ui.managment.DialogType

sealed class BankCardViewingAction : MviAction {
    data object ClickButtonBack : BankCardViewingAction()

    data class OpenDialog(val type: DialogType) : BankCardViewingAction()

    data object CloseDialog : BankCardViewingAction()

    data object Edit : BankCardViewingAction()

    data class CopyText(val text: AnnotatedString) : BankCardViewingAction()

    data class ShowSnackbar(val message: String) : BankCardViewingAction()

    data class Delete(val onSuccess: () -> Unit = {}) : BankCardViewingAction()
}


sealed class BankCardEditingAction : MviAction {
    data class ShowSnackbar(val message: String) : BankCardEditingAction()

    data class Save(val onSuccess: () -> Unit = {}) : BankCardEditingAction()

    data object ClickButtonBack : BankCardEditingAction()

    data class OpenDialog(val type: DialogType) : BankCardEditingAction()

    data object CloseDialog : BankCardEditingAction()

    data object ShowPaymentSystemSelection : BankCardEditingAction()

    data object HidePaymentSystemSelection : BankCardEditingAction()
}