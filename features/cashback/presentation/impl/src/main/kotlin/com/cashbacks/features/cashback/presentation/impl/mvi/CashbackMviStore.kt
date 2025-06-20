package com.cashbacks.features.cashback.presentation.impl.mvi

import androidx.compose.runtime.Immutable
import com.cashbacks.common.composables.management.DialogType
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.features.bankcard.domain.model.BasicBankCard
import com.cashbacks.features.bankcard.presentation.api.BankCardArgs
import com.cashbacks.features.cashback.domain.model.CashbackOwner
import com.cashbacks.features.cashback.domain.model.FullCashback
import com.cashbacks.features.cashback.domain.model.MeasureUnit
import com.cashbacks.features.cashback.presentation.impl.mvi.model.EditableCashback
import com.cashbacks.features.shop.presentation.api.ShopArgs
import kotlinx.serialization.Serializable

internal sealed interface CashbackAction {
    data object StartLoading : CashbackAction
    data object FinishLoading : CashbackAction
    data class UpdateEditableCashback(val cashback: EditableCashback) : CashbackAction
    data class LoadCashback(val cashback: FullCashback) : CashbackAction
    data class LoadOwner(val owner: CashbackOwner) : CashbackAction
    data class DisplayMessage(val message: String) : CashbackAction
}


internal sealed interface CashbackLabel {
    data class DisplayMessage(val message: String) : CashbackLabel
    data object NavigateBack : CashbackLabel
    data class NavigateToShop(val args: ShopArgs) : CashbackLabel
    data class NavigateToBankCard(val args: BankCardArgs) : CashbackLabel
    data class UpdateOpenedDialog(val type: DialogType?) : CashbackLabel
    data object ScrollToEnd : CashbackLabel
}


internal sealed interface CashbackIntent {
    data object ClickButtonBack : CashbackIntent
    data class UpdateErrorMessage(val error: CashbackError) : CashbackIntent
    data class UpdateCashback(val block: EditableCashback.() -> EditableCashback) : CashbackIntent
    data object StartCreatingCategory : CashbackIntent
    data object CancelCreatingCategory : CashbackIntent
    data class AddCategory(val name: String) : CashbackIntent
    data object CreateShop : CashbackIntent
    data object CreateBankCard : CashbackIntent
    data class SaveData(val onSuccess: () -> Unit = {}) : CashbackIntent
    data class DeleteData(val onSuccess: () -> Unit = {}) : CashbackIntent
    data class ShowDialog(val type: DialogType) : CashbackIntent
    data object HideDialog : CashbackIntent
    data object ShowBankCardsSelection : CashbackIntent
    data object HideBankCardsSelection : CashbackIntent
    data object ShowOwnersSelection : CashbackIntent
    data object HideOwnersSelection : CashbackIntent
    data object HideAllSelections : CashbackIntent
    data object ShowMeasureUnitsSelection : CashbackIntent
    data object ShowKeyboard : CashbackIntent
}


internal sealed interface CashbackMessage {
    data class UpdateScreenState(val state: ScreenState) : CashbackMessage
    data class SetInitialCashback(val cashback: EditableCashback) : CashbackMessage
    data class UpdateCashback(val cashback: EditableCashback) : CashbackMessage
    data class UpdateIsCreatingCategory(val isCreatingCategory: Boolean) : CashbackMessage
    data class UpdateShowingOwnersSelection(val showSelection: Boolean) : CashbackMessage
    data class UpdateShowingBankCardsSelection(val showSelection: Boolean) : CashbackMessage
    data class UpdateShowingErrors(val showErrors: Boolean) : CashbackMessage
    data class SetErrorMessage(val error: CashbackError, val message: String?) : CashbackMessage
    data class SetErrorMessages(val errorMessages: Map<CashbackError, String>) : CashbackMessage
    data class UpdateSelectionOwners(val owners: List<CashbackOwner>?) : CashbackMessage
    data class UpdateSelectionCards(val cards: List<BasicBankCard>?) : CashbackMessage
    data class UpdateSelectionMeasureUnits(val units: List<MeasureUnit>?) : CashbackMessage
}


@Serializable
@Immutable
internal data class CashbackState(
    val screenState: ScreenState = ScreenState.Stable,
    val initialCashback: EditableCashback = EditableCashback(),
    val cashback: EditableCashback = EditableCashback(),
    val showBankCardsSelection: Boolean = false,
    val showOwnersSelection: Boolean = false,
    val selectionOwners: List<CashbackOwner>? = null,
    val selectionBankCards: List<BasicBankCard>? = null,
    val selectionMeasureUnits: List<MeasureUnit>? = null,
    val isCreatingCategory: Boolean = false,
    val showErrors: Boolean = false,
    val errors: Map<CashbackError, String> = emptyMap()
) {
    fun isCashbackChanged(): Boolean = initialCashback != cashback
}


internal enum class CashbackError {
    Owner,
    BankCard,
    Amount
}