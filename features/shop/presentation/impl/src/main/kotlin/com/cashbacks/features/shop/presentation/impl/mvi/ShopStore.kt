package com.cashbacks.features.shop.presentation.impl.mvi

import androidx.compose.runtime.Immutable
import com.cashbacks.common.utils.management.DialogType
import com.cashbacks.common.utils.management.ScreenState
import com.cashbacks.common.utils.management.ViewModelState
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.presentation.api.CashbackArgs
import com.cashbacks.features.category.domain.model.Category
import com.cashbacks.features.shop.domain.model.CategoryShop
import com.cashbacks.features.shop.presentation.impl.mvi.model.EditableShop
import kotlinx.serialization.Serializable

internal sealed interface ShopAction {
    data object StartLoading : ShopAction
    data object FinishLoading : ShopAction
    data class DisplayMessage(val message: String) : ShopAction
    data class LoadShop(val shop: CategoryShop) : ShopAction
    data class LoadCashbacks(val cashbacks: List<Cashback>) : ShopAction
}


internal sealed interface ShopLabel {
    data class DisplayMessage(val message: String) : ShopLabel
    data object NavigateBack : ShopLabel
    data class NavigateToCashback(val args: CashbackArgs) : ShopLabel
    data class ChangeOpenedDialog(val openedDialogType: DialogType?) : ShopLabel
}


internal sealed interface ShopIntent {
    data object ClickButtonBack : ShopIntent
    data object ClickEditButton : ShopIntent
    data class Save(val onSuccess: () -> Unit = {}) : ShopIntent
    data class Delete(val onSuccess: suspend () -> Unit = {}) : ShopIntent
    data class DeleteCashback(val cashback: Cashback) : ShopIntent
    data object StartCreatingCategory : ShopIntent
    data object CancelCreatingCategory : ShopIntent
    data class AddCategory(val name: String) : ShopIntent

    data class SwipeCashback(val position: Int?) : ShopIntent {
        constructor(position: Int, isSwiped: Boolean) : this(position.takeIf { isSwiped })
    }

    data object CreateCashback : ShopIntent
    data class NavigateToCashback(val cashbackId: Long) : ShopIntent
    data class OpenDialog(val type: DialogType) : ShopIntent
    data object CloseDialog : ShopIntent
    data object ShowCategoriesSelection : ShopIntent
    data object HideCategoriesSelection : ShopIntent
    data class UpdateShopName(val name: String) : ShopIntent
    data class UpdateShopParent(val parent: Category) : ShopIntent
    data class UpdateErrorMessage(val error: ShopError) : ShopIntent
}


internal sealed interface ShopMessage {
    data class UpdateScreenState(val state: ScreenState) : ShopMessage
    data class UpdateViewModelState(val state: ViewModelState) : ShopMessage
    data class SetInitialShop(val shop: EditableShop) : ShopMessage
    data class UpdateShop(val shop: EditableShop) : ShopMessage
    data class UpdateCashbacks(val cashbacks: List<Cashback>) : ShopMessage
    data class ChangeSelectedCashbackIndex(val index: Int?) : ShopMessage
    data class SetIsCreatingCategory(val isCreatingCategory: Boolean) : ShopMessage
    data class SetErrorMessage(val error: ShopError, val message: String?) : ShopMessage
    data class SetErrorMessages(val errors: Map<ShopError, String>) : ShopMessage
    data class UpdateShowingErrors(val showErrors: Boolean) : ShopMessage
    data class SetShowingCategoriesSelection(val isShowing: Boolean) : ShopMessage
    data class UpdateSelectionCategories(val categories: List<Category>?) : ShopMessage
}


@Serializable
@Immutable
internal data class ShopState(
    val screenState: ScreenState = ScreenState.Stable,
    val viewModelState: ViewModelState = ViewModelState.Viewing,
    val initialShop: EditableShop = EditableShop(),
    val shop: EditableShop = EditableShop(),
    val cashbacks: List<Cashback> = emptyList(),
    val showCategoriesSelection: Boolean = false,
    val selectionCategories: List<Category>? = null,
    val isCreatingCategory: Boolean = false,
    val selectedCashbackIndex: Int? = null,
    val errors: Map<ShopError, String> = emptyMap(),
    val showErrors: Boolean = false
) {
    fun isShopChanged() = initialShop != shop
}


internal enum class ShopError {
    Parent,
    Name
}