package com.cashbacks.app.ui.features.cashback.mvi

import com.cashbacks.app.model.CashbackError
import com.cashbacks.app.mvi.MviAction
import com.cashbacks.app.ui.managment.DialogType

sealed class CashbackAction : MviAction {
    data object ClickButtonBack : CashbackAction()

    internal data class UpdateCashbackErrorMessage(val error: CashbackError) : CashbackAction()

    data object StartCreatingCategory : CashbackAction()

    data object CancelCreatingCategory : CashbackAction()

    data class AddCategory(val name: String) : CashbackAction()

    data object CreateShop : CashbackAction()

    data object CreateBankCard : CashbackAction()

    data class SaveData(val onSuccess: () -> Unit = {}) : CashbackAction()

    data class DeleteData(val onSuccess: () -> Unit = {}) : CashbackAction()

    data class ShowDialog(val type: DialogType) : CashbackAction()

    data object HideDialog : CashbackAction()

    data object ShowBankCardsSelection : CashbackAction()

    data object HideBankCardsSelection : CashbackAction()

    data object ShowOwnersSelection : CashbackAction()

    data object HideOwnersSelection : CashbackAction()

    data object HideAllSelections : CashbackAction()
}