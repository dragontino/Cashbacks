package com.cashbacks.app.ui.features.home.cashbacks.mvi

import com.cashbacks.app.mvi.MviAction
import com.cashbacks.app.ui.features.cashback.CashbackArgs
import com.cashbacks.app.ui.features.home.HomeTopAppBarState
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.domain.model.Cashback

sealed class CashbacksAction : MviAction {
    internal data class UpdateAppBarState(val state: HomeTopAppBarState) : CashbacksAction()

    data object ClickButtonBack : CashbacksAction()

    data object ClickNavigationButton : CashbacksAction()

    data object OpenBottomSheet : CashbacksAction()

    data object CloseBottomSheet : CashbacksAction()

    data class OpenDialog(val type: DialogType) : CashbacksAction()

    data object CloseDialog : CashbacksAction()

    data class NavigateToCashback(val args: CashbackArgs) : CashbacksAction()

    data class DeleteCashback(val cashback: Cashback) : CashbacksAction()

    data class SwipeCashback(
        val isOpened: Boolean,
        val position: Int? = null
    ) : CashbacksAction()
}