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
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.serialization.Serializable

internal sealed interface CategoriesAction {
    data class LoadCategories(val categories: ImmutableMap<Category, Set<Cashback>>?) : CategoriesAction
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

    data class SwipeCategory(val position: Int? = null) : CategoriesIntent() {
        constructor(position: Int, isSwiped: Boolean) : this(position.takeIf { isSwiped })
    }
}


internal sealed interface CategoriesMessage {
    data class UpdateScreenState(val state: ScreenState) : CategoriesMessage
    data class UpdateViewModelState(val state: ViewModelState) : CategoriesMessage
    data class UpdateAppBarState(val state: HomeTopAppBarState) : CategoriesMessage
    data class UpdateCategories(val categories: Map<Category, Set<Cashback>>?) : CategoriesMessage
    data class UpdateIsCreatingCategory(val isCreatingCategory: Boolean) : CategoriesMessage
    data class UpdateSelectedCategoryIndex(val index: Int?) : CategoriesMessage
}


@Serializable
@Immutable
internal data class CategoriesState(
    val screenState: ScreenState = ScreenState.Stable,
    val viewModelState: ViewModelState = ViewModelState.Viewing,
    val appBarState: HomeTopAppBarState = HomeTopAppBarState.TopBar,
    val categories: Map<Category, Set<Cashback>>? = emptyMap(),
    val isCreatingCategory: Boolean = false,
    val selectedCategoryIndex: Int? = null
)