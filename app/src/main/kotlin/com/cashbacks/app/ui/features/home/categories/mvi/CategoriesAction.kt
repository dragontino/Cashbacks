package com.cashbacks.app.ui.features.home.categories.mvi

import com.cashbacks.app.mvi.MviAction
import com.cashbacks.app.ui.features.category.CategoryArgs
import com.cashbacks.app.ui.features.home.HomeTopAppBarState
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.domain.model.Category

sealed class CategoriesAction : MviAction {
    data object StartEdit : CategoriesAction()
    data object FinishEdit : CategoriesAction()
    data object SwitchEdit : CategoriesAction()

    data object ClickButtonBack : CategoriesAction()

    internal data class NavigateToCategory(
        val args: CategoryArgs,
        val isEditing: Boolean = false
    ) : CategoriesAction()

    data object StartCreatingCategory : CategoriesAction()

    data object FinishCreatingCategory : CategoriesAction()

    data class AddCategory(val name: String) : CategoriesAction()

    data class DeleteCategory(val category: Category) : CategoriesAction()

    data class OpenDialog(val type: DialogType) : CategoriesAction()

    data object CloseDialog : CategoriesAction()

    internal data class UpdateAppBarState(val state: HomeTopAppBarState) : CategoriesAction()

    data object ScrollToEnd : CategoriesAction()

    data class SwipeCategory(
        val isOpened: Boolean,
        val position: Int? = null
    ) : CategoriesAction()
}