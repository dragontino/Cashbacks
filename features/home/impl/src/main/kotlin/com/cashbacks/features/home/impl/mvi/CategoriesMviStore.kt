package com.cashbacks.features.home.impl.mvi

import androidx.compose.runtime.Immutable
import com.cashbacks.common.composables.management.DialogType
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.common.composables.management.ViewModelState
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.presentation.api.CashbackArgs
import com.cashbacks.features.category.domain.model.Category
import com.cashbacks.features.category.presentation.api.CategoryArgs
import com.cashbacks.features.home.impl.composables.HomeTopAppBarState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

internal sealed interface CategoriesAction {
    data class LoadCategories(val categories: List<CategoryWithCashback>?) : CategoriesAction
}


internal sealed interface CategoriesLabel {
    data class DisplayMessage(val message: String) : CategoriesLabel
    data class NavigateToCategory(val args: CategoryArgs) : CategoriesLabel
    data class NavigateToCashback(val args: CashbackArgs) : CategoriesLabel
    data object OpenNavigationDrawer : CategoriesLabel
    data object NavigateBack : CategoriesLabel
    data class ChangeOpenedDialog(val type: DialogType?) : CategoriesLabel
    data object ScrollToEnd : CategoriesLabel
}


@Immutable
internal sealed class CategoriesIntent {
    data object StartEdit : CategoriesIntent()
    data object FinishEdit : CategoriesIntent()
    data object SwitchEdit : CategoriesIntent()

    data object ClickButtonBack : CategoriesIntent()
    data object ClickNavigationButton : CategoriesIntent()

    data class NavigateToCategory(val args: CategoryArgs) : CategoriesIntent()
    data class NavigateToCashback(val args: CashbackArgs) : CategoriesIntent()
    
    data object StartCreatingCategory : CategoriesIntent()
    data object FinishCreatingCategory : CategoriesIntent()
    data class AddCategory(val name: String) : CategoriesIntent()
    data class DeleteCategory(val category: Category) : CategoriesIntent()

    data class OpenDialog(val type: DialogType) : CategoriesIntent()
    data object CloseDialog : CategoriesIntent()
    data class ChangeAppBarState(val state: HomeTopAppBarState) : CategoriesIntent()
    data object ScrollToEnd : CategoriesIntent()

    data class SwipeCategory(val id: String? = null) : CategoriesIntent() {
        constructor(id: String, isSwiped: Boolean) : this(id.takeIf { isSwiped })
    }

    data class SelectCategory(val id: String? = null) : CategoriesIntent() {
        constructor(id: String, isSelected: Boolean) : this(id.takeIf { isSelected })
    }
}


internal sealed interface CategoriesMessage {
    data class UpdateScreenState(val state: ScreenState) : CategoriesMessage
    data class UpdateViewModelState(val state: ViewModelState) : CategoriesMessage
    data class UpdateAppBarState(val state: HomeTopAppBarState) : CategoriesMessage
    data class UpdateCategories(val categories: ImmutableList<CategoryWithCashback>?) : CategoriesMessage
    data class UpdateIsCreatingCategory(val isCreatingCategory: Boolean) : CategoriesMessage
    data class UpdateSwipedCategoryId(val id: String?) : CategoriesMessage
    data class UpdateSelectedCategoryId(val id: String?) : CategoriesMessage
}


@Serializable
@Immutable
internal data class CategoriesState(
    val screenState: ScreenState = ScreenState.Stable,
    val viewModelState: ViewModelState = ViewModelState.Viewing,
    val appBarState: HomeTopAppBarState = HomeTopAppBarState.TopBar,
    val categories: ImmutableList<CategoryWithCashback>? = persistentListOf(),
    val isCreatingCategory: Boolean = false,
    val swipedCategoryId: String? = null,
    val selectedCategoryId: String? = null
)


@Serializable
@Immutable
internal data class CategoryWithCashback(
    val category: Category,
    val maxCashback: Cashback?
) {
    val id: String get() = when(maxCashback) {
        null -> category.id.toString()
        else -> "${category.id}-${maxCashback.id}"
    }
}