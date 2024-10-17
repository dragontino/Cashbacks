package com.cashbacks.app.ui.features.category.mvi

import com.cashbacks.app.mvi.MviAction
import com.cashbacks.app.ui.features.category.CategoryTabItemType
import com.cashbacks.app.ui.features.shop.ShopArgs
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.domain.model.BasicCashback
import com.cashbacks.domain.model.BasicShop

sealed interface CategoryAction : MviAction {
    sealed interface Viewing : CategoryAction
    sealed interface Editing : CategoryAction

    data object ClickButtonBack : Viewing, Editing

    data class DeleteCashback(val cashback: BasicCashback) : Viewing, Editing

    data class DeleteShop(val shop: BasicShop) : Viewing, Editing

    data class OpenDialog(val type: DialogType) : Viewing, Editing

    data object CloseDialog : Viewing, Editing

    data class SwipeShop(val position: Int, val isOpened: Boolean) : Viewing, Editing

    data class SwipeCashback(val position: Int, val isOpened: Boolean) : Viewing, Editing

    data class NavigateToShop(val args: ShopArgs) : Viewing, Editing

    data class NavigateToCashback(val cashbackId: Long?) : Viewing, Editing


    data class NavigateToCategoryEditing(val startTab: CategoryTabItemType) : Viewing



    data class NavigateToCategoryViewing(val startTab: CategoryTabItemType) : Editing

    data class SaveCategory(val onSuccess: () -> Unit = {}) : Editing

    data class DeleteCategory(val onSuccess: () -> Unit = {}) : Editing

    data object StartCreateShop : Editing

    data object FinishCreateShop : Editing

    data class SaveShop(val name: String) : Editing
}