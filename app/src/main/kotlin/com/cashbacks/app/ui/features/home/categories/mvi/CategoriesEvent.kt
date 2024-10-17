package com.cashbacks.app.ui.features.home.categories.mvi

import com.cashbacks.app.mvi.MviEvent
import com.cashbacks.app.ui.features.category.CategoryArgs
import com.cashbacks.app.ui.managment.DialogType

sealed class CategoriesEvent : MviEvent {
    data class ShowSnackbar(val message: String) : CategoriesEvent()

    internal data class NavigateToCategory(
        val args: CategoryArgs,
        val isEditing: Boolean
    ) : CategoriesEvent()

    data object NavigateBack : CategoriesEvent()

    data class OpenDialog(val type: DialogType) : CategoriesEvent()

    data object CloseDialog : CategoriesEvent()

    data object ScrollToEnd : CategoriesEvent()
}