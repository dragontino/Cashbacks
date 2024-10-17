package com.cashbacks.app.ui.features.home.cards.mvi

import com.cashbacks.app.mvi.MviAction
import com.cashbacks.app.ui.features.home.HomeTopAppBarState

sealed class CardsAction : MviAction {
    data class ShowSnackbar(val message: String) : CardsAction()

    internal data class UpdateAppBarState(val state: HomeTopAppBarState) : CardsAction()

    data class OpenBankCardDetails(val cardId: Long) : CardsAction()

    data object CreateBankCard : CardsAction()

    data object ClickNavigationIcon : CardsAction()
}