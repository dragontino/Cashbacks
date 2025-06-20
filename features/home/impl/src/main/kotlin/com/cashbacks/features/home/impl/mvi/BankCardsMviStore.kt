package com.cashbacks.features.home.impl.mvi

import androidx.compose.runtime.Immutable
import com.cashbacks.common.composables.management.DialogType
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.features.bankcard.domain.model.BankCard
import com.cashbacks.features.bankcard.domain.model.BasicBankCard
import com.cashbacks.features.bankcard.presentation.api.BankCardArgs
import com.cashbacks.features.home.impl.composables.HomeTopAppBarState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable

internal sealed interface BankCardsAction {
    data class LoadBankCards(val cards: List<BankCard>?) : BankCardsAction
}


internal sealed interface BankCardsLabel {
    data class DisplayMessage(val message: String) : BankCardsLabel
    data class NavigateToBankCard(val args: BankCardArgs) : BankCardsLabel
    data object OpenNavigationDrawer : BankCardsLabel
    data class ChangeOpenedDialog(val type: DialogType?) : BankCardsLabel
}


internal sealed interface BankCardsIntent {
    data class DisplayMessage(val message: String) : BankCardsIntent

    data object ClickNavigationButton : BankCardsIntent

    data class OpenBankCardDetails(val cardId: Long) : BankCardsIntent
    data object CreateBankCard : BankCardsIntent
    data class EditBankCard(val cardId: Long) : BankCardsIntent
    data class DeleteBankCard(val card: BasicBankCard) : BankCardsIntent

    data class OpenDialog(val type: DialogType) : BankCardsIntent
    data object CloseDialog : BankCardsIntent

    data class ChangeAppBarState(val state: HomeTopAppBarState) : BankCardsIntent

    data class SwipeCard(val position: Int? = null) : BankCardsIntent {
        constructor(position: Int, isSwiped: Boolean) : this(position.takeIf { isSwiped })
    }
    data class ExpandCard(val position: Int? = null) : BankCardsIntent {
        constructor(position: Int, isExpanded: Boolean) : this(position.takeIf { isExpanded })
    }
}


internal sealed interface BankCardsMessage {
    data class UpdateScreenState(val state: ScreenState) : BankCardsMessage
    data class UpdateAppBarState(val state: HomeTopAppBarState) : BankCardsMessage
    data class UpdateBankCards(val cards: List<BankCard>?) : BankCardsMessage
    data class UpdateSwipedCardIndex(val index: Int?) : BankCardsMessage
    data class UpdateExpandedCardIndex(val index: Int?) : BankCardsMessage
}


@Serializable
@Immutable
internal data class BankCardsState(
    val screenState: ScreenState = ScreenState.Stable,
    val appBarState: HomeTopAppBarState = HomeTopAppBarState.TopBar,
    val cards: List<BankCard>? = emptyList(),
    val swipedCardIndex: Int? = null,
    val expandedCardIndex: Int? = null
)