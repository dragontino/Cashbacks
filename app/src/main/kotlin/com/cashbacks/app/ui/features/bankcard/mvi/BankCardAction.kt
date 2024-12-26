package com.cashbacks.app.ui.features.bankcard.mvi

import androidx.compose.ui.text.AnnotatedString
import com.cashbacks.app.model.BankCardError
import com.cashbacks.app.mvi.MviAction
import com.cashbacks.app.ui.managment.BottomSheetType
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

    data class ShowDialog(val type: DialogType) : BankCardEditingAction()

    data object HideDialog : BankCardEditingAction()

    data object ShowPaymentSystemSelection : BankCardEditingAction()

    data object HidePaymentSystemSelection : BankCardEditingAction()

    data class ShowBottomSheet(val type: BottomSheetType) : BankCardEditingAction()

    data object HideBottomSheet : BankCardEditingAction()

    internal data class UpdateErrorMessage(val error: BankCardError) : BankCardEditingAction()
}