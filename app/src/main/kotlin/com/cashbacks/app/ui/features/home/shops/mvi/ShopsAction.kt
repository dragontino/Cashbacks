package com.cashbacks.app.ui.features.home.shops.mvi

import com.cashbacks.app.mvi.MviAction
import com.cashbacks.app.ui.features.home.HomeTopAppBarState
import com.cashbacks.app.ui.features.shop.ShopArgs
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.domain.model.Shop

sealed class ShopsAction : MviAction {
    data object StartEdit : ShopsAction()
    data object FinishEdit : ShopsAction()
    data object SwitchEdit : ShopsAction()

    data object ClickButtonBack : ShopsAction()

    data class NavigateToShop(val args: ShopArgs) : ShopsAction()

    data class DeleteShop(val shop: Shop) : ShopsAction()

    data class OpenDialog(val type: DialogType) : ShopsAction()

    data object CloseDialog : ShopsAction()

    internal data class UpdateAppBarState(val state: HomeTopAppBarState) : ShopsAction()

    data class SwipeShop(
        val isOpened: Boolean,
        val position: Int? = null
    ) : ShopsAction()
}