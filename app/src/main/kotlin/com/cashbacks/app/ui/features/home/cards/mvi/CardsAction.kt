package com.cashbacks.app.ui.features.home.cards.mvi

import com.cashbacks.app.mvi.MviAction
import com.cashbacks.app.ui.features.home.HomeTopAppBarState
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.domain.model.BasicBankCard

sealed class CardsAction : MviAction {
    data class ShowSnackbar(val message: String) : CardsAction()

    internal data class UpdateAppBarState(val state: HomeTopAppBarState) : CardsAction()

    data class OpenBankCardDetails(val cardId: Long) : CardsAction()

    data class EditBankCard(val cardId: Long) : CardsAction()

    data class DeleteBankCard(val card: BasicBankCard) : CardsAction()

    data class SwipeCard(
        val isSwiped: Boolean,
        val cardId: Long? = null
    ) : CardsAction()

    data class ExpandCard(
        val isExpanded: Boolean,
        val cardId: Long? = null
    ) : CardsAction()

    data object CreateBankCard : CardsAction()

    data object ClickNavigationIcon : CardsAction()

    data class ShowDialog(val type: DialogType) : CardsAction()

    data object HideDialog : CardsAction()
}