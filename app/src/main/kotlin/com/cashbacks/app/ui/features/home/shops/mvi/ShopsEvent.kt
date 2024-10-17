package com.cashbacks.app.ui.features.home.shops.mvi

import com.cashbacks.app.mvi.MviEvent
import com.cashbacks.app.ui.features.shop.ShopArgs
import com.cashbacks.app.ui.managment.DialogType

sealed class ShopsEvent : MviEvent {
    data class ShowSnackbar(val message: String) : ShopsEvent()

    data class NavigateToShop(val args: ShopArgs) : ShopsEvent()

    data object NavigateBack : ShopsEvent()

    data class OpenDialog(val type: DialogType) : ShopsEvent()

    data object CloseDialog : ShopsEvent()
}