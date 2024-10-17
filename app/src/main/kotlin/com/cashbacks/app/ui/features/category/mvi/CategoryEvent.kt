package com.cashbacks.app.ui.features.category.mvi

import com.cashbacks.app.mvi.MviEvent
import com.cashbacks.app.ui.features.cashback.CashbackArgs
import com.cashbacks.app.ui.features.category.CategoryArgs
import com.cashbacks.app.ui.features.shop.ShopArgs
import com.cashbacks.app.ui.managment.DialogType

sealed interface CategoryEvent : MviEvent {
    sealed interface Viewing : CategoryEvent
    sealed interface Editing : CategoryEvent

    data object NavigateBack : Viewing, Editing

    data class ShowSnackbar(val message: String) : Viewing, Editing

    data class NavigateToShopScreen(val args: ShopArgs) : Viewing, Editing

    data class NavigateToCashbackScreen(val args: CashbackArgs) : Viewing, Editing

    data class OpenDialog(val type: DialogType) : Viewing, Editing

    data object CloseDialog : Viewing, Editing



    data class NavigateToCategoryEditingScreen(val args: CategoryArgs) : Viewing



    data class NavigateToCategoryViewingScreen(val args: CategoryArgs) : Editing
}