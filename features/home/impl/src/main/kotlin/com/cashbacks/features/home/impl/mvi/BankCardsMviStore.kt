package com.cashbacks.features.home.impl.mvi

import androidx.compose.runtime.Immutable
import com.cashbacks.common.composables.management.DialogType
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.features.bankcard.domain.model.BankCard
import com.cashbacks.features.bankcard.domain.model.BasicBankCard
import com.cashbacks.features.bankcard.presentation.api.BankCardArgs
import com.cashbacks.features.home.impl.composables.HomeTopAppBarState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
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

    data class SwipeCard(val id: Long? = null) : BankCardsIntent {
        constructor(id: Long, isSwiped: Boolean) : this(id.takeIf { isSwiped })
    }
    data class ExpandCard(val id: Long? = null) : BankCardsIntent {
        constructor(id: Long, isExpanded: Boolean) : this(id.takeIf { isExpanded })
    }
}


internal sealed interface BankCardsMessage {
    data class UpdateScreenState(val state: ScreenState) : BankCardsMessage
    data class UpdateAppBarState(val state: HomeTopAppBarState) : BankCardsMessage
    data class UpdateBankCards(val cards: ImmutableList<BankCard>?) : BankCardsMessage
    data class UpdateSwipedCardId(val id: Long?) : BankCardsMessage
    data class UpdateExpandedCardId(val id: Long?) : BankCardsMessage
}


@Serializable
@Immutable
internal data class BankCardsState(
    val screenState: ScreenState = ScreenState.Stable,
    val appBarState: HomeTopAppBarState = HomeTopAppBarState.TopBar,
    val cards: ImmutableList<BankCard>? = persistentListOf(),
    val swipedCardId: Long? = null,
    val expandedCardId: Long? = null
)