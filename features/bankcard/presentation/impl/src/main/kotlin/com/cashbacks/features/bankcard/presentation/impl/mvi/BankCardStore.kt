package com.cashbacks.features.bankcard.presentation.impl.mvi

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import com.cashbacks.common.composables.management.BottomSheetType
import com.cashbacks.common.composables.management.DialogType
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.features.bankcard.domain.model.FullBankCard
import com.cashbacks.features.bankcard.presentation.api.BankCardArgs
import com.cashbacks.features.bankcard.presentation.impl.mvi.model.EditableBankCard


internal sealed interface BankCardAction {
    data object LoadStarted : BankCardAction

    data object LoadFinished : BankCardAction

    data class LoadBankCard(val card: FullBankCard) : BankCardAction

    data class DisplayMessage(val message: String) : BankCardAction
}


internal sealed interface ViewingLabel {
    data class DisplayMessage(val message: String) : ViewingLabel

    data class CopyText(val text: AnnotatedString) : ViewingLabel

    data class NavigateToEditingBankCard(val args: BankCardArgs) : ViewingLabel

    data class ChangeOpenedDialog(val openedDialogType: DialogType?) : ViewingLabel

    data object NavigateBack : ViewingLabel
}


internal sealed interface EditingLabel {
    data class DisplayMessage(val message: String) : EditingLabel

    data object NavigateBack : EditingLabel

    data class ChangeOpenedDialog(val openedDialogType: DialogType?) : EditingLabel

    data class ChangeOpenedBottomSheet(val type: BottomSheetType?) : EditingLabel
}


internal sealed interface ViewingIntent {
    data object ClickButtonBack : ViewingIntent

    data class DisplayMessage(val message: String) : ViewingIntent

    data class OpenDialog(val type: DialogType) : ViewingIntent

    data object CloseDialog : ViewingIntent

    data object Edit : ViewingIntent

    data class CopyText(val text: AnnotatedString) : ViewingIntent

    data class Delete(val onSuccess: () -> Unit = {}) : ViewingIntent
}


internal sealed interface EditingIntent {
    data class DisplayMessage(val message: String) : EditingIntent

    data class Save(val onSuccess: () -> Unit = {}) : EditingIntent

    data object ClickButtonBack : EditingIntent

    data class ShowDialog(val type: DialogType) : EditingIntent

    data object HideDialog : EditingIntent

    data object ShowPaymentSystemSelection : EditingIntent

    data object HidePaymentSystemSelection : EditingIntent

    data class ShowBottomSheet(val type: BottomSheetType) : EditingIntent

    data object HideBottomSheet : EditingIntent

    data class UpdateBankCard(val card: EditableBankCard) : EditingIntent

    data class UpdateErrorMessage(val error: BankCardError) : EditingIntent
}


internal sealed interface ViewingMessage {
    data class UpdateScreenState(val state: ScreenState) : ViewingMessage

    data class UpdateBankCard(val bankCard: FullBankCard) : ViewingMessage
}


internal sealed interface EditingMessage {

    data class UpdateScreenState(val state: ScreenState) : EditingMessage

    data class UpdateBankCard(val bankCard: EditableBankCard) : EditingMessage

    data class SetErrorMessage(val error: BankCardError, val message: String?) : EditingMessage

    data class SetErrorMessages(val messages: Map<BankCardError, String>) : EditingMessage

    data class SetInitialBankCard(val bankCard: FullBankCard) : EditingMessage

    data class UpdateShowingErrors(val showErrors: Boolean) : EditingMessage

    data class UpdateShowingPaymentSystemSelection(val show: Boolean) : EditingMessage
}


@Immutable
internal data class EditingState(
    val screenState: ScreenState = ScreenState.Stable,
    val initialCard: FullBankCard = FullBankCard(),
    val card: EditableBankCard = EditableBankCard(),
    val errors: Map<BankCardError, String> = emptyMap(),
    val showErrors: Boolean = false,
    val showPaymentSystemSelection: Boolean = false
) {
    fun isChanged(): Boolean {
        return initialCard != card.mapToBankCard()
    }
}


internal data class ViewingState(
    val screenState: ScreenState = ScreenState.Stable,
    val card: FullBankCard = FullBankCard(),
)


internal enum class BankCardError {
    Number,
    ValidityPeriod,
    Cvv,
    Pin
}