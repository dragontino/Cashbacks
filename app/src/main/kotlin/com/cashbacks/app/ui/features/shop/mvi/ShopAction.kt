package com.cashbacks.app.ui.features.shop.mvi

import com.cashbacks.app.mvi.MviAction
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.domain.model.Cashback

sealed class ShopAction : MviAction {

    data object ClickButtonBack : ShopAction()

    data object Edit : ShopAction()

    data class Save(val onSuccess: () -> Unit = {}) : ShopAction()

    data class Delete(val onSuccess: () -> Unit = {}) : ShopAction()

    data class DeleteCashback(val cashback: Cashback) : ShopAction()

    data object StartCreatingCategory : ShopAction()

    data object CancelCreatingCategory : ShopAction()

    data class AddCategory(val name: String) : ShopAction()

    data class SwipeCashback(val isOpened: Boolean, val position: Int? = null) : ShopAction()

    data object CreateCashback : ShopAction()

    data class NavigateToCashback(val cashbackId: Long) : ShopAction()

    data class OpenDialog(val type: DialogType) : ShopAction()

    data object CloseDialog : ShopAction()

    data object ShowCategoriesSelection : ShopAction()

    data object HideCategoriesSelection : ShopAction()
}