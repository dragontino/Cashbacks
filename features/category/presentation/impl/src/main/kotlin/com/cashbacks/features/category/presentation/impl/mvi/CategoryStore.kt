package com.cashbacks.features.category.presentation.impl.mvi

import androidx.compose.runtime.Immutable
import com.cashbacks.common.utils.management.DialogType
import com.cashbacks.common.utils.management.ScreenState
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.presentation.api.CashbackArgs
import com.cashbacks.features.category.domain.model.Category
import com.cashbacks.features.category.presentation.api.CategoryArgs
import com.cashbacks.features.category.presentation.api.CategoryTabItemType
import com.cashbacks.features.shop.domain.model.Shop
import com.cashbacks.features.shop.presentation.api.ShopArgs

// Actions
internal sealed interface EditingAction {
    data class LoadInitialCategory(val category: Category) : EditingAction
}

internal sealed interface CategoryAction : EditingAction {
    data object StartLoading : CategoryAction
    data object FinishLoading : CategoryAction
    data class LoadCategory(val category: Category) : CategoryAction
    data class LoadShops(val shops: Map<Shop, Set<Cashback>>) : CategoryAction
    data class LoadCashbacks(val cashbacks: List<Cashback>) : CategoryAction
    data class DisplayMessage(val message: String) : CategoryAction
}


// Labels
internal sealed interface ViewingLabel {
    data class NavigateToCategoryEditingScreen(val args: CategoryArgs) : ViewingLabel
}

internal sealed interface EditingLabel {
    data class NavigateToCategoryViewingScreen(val args: CategoryArgs) : EditingLabel
}

internal sealed interface CategoryLabel : ViewingLabel, EditingLabel {
    data object NavigateBack : CategoryLabel
    data class DisplayMessage(val message: String) : CategoryLabel
    data class NavigateToShopScreen(val args: ShopArgs) : CategoryLabel
    data class NavigateToCashbackScreen(val args: CashbackArgs) : CategoryLabel
    data class OpenDialog(val type: DialogType) : CategoryLabel
    data object CloseDialog : CategoryLabel
}


// Intents
internal sealed interface ViewingIntent {
    data class NavigateToCategoryEditing(val startTab: CategoryTabItemType) : ViewingIntent
    data class DeleteCashback(val cashback: Cashback) : ViewingIntent
    data class DeleteShop(val shop: Shop) : ViewingIntent
    data class NavigateToShop(val shopId: Long) : ViewingIntent
    data class NavigateToCashback(val cashbackId: Long?) : ViewingIntent
}

internal sealed interface EditingIntent {
    data class NavigateToCategoryViewing(val startTab: CategoryTabItemType) : EditingIntent
    data class UpdateCategoryName(val name: String) : EditingIntent
    data class DeleteCashback(val cashback: Cashback) : EditingIntent
    data class DeleteShop(val shop: Shop) : EditingIntent
    data class SaveCategory(val onSuccess: () -> Unit = {}) : EditingIntent
    data class DeleteCategory(val onSuccess: suspend () -> Unit = {}) : EditingIntent
    data object StartCreatingShop : EditingIntent
    data object FinishCreatingShop : EditingIntent
    data class SaveShop(val name: String) : EditingIntent
    data class ClickToShop(val shopId: Long) : EditingIntent
    data object CreateCashback : EditingIntent
    data class ClickToCashback(val cashbackId: Long) : EditingIntent
    data class UpdateErrorMessage(val error: CategoryError) : EditingIntent
}

internal sealed interface CategoryIntent : ViewingIntent, EditingIntent {
    data object ClickButtonBack : CategoryIntent

    data class OpenDialog(val type: DialogType) : CategoryIntent

    data object CloseDialog : CategoryIntent

    data class SwipeShop(val position: Int?) : CategoryIntent {
        constructor(position: Int, isSwiped: Boolean) : this(position.takeIf { isSwiped })
    }

    data class SwipeCashback(val position: Int?) : CategoryIntent {
        constructor(position: Int, isSwiped: Boolean) : this(position.takeIf { isSwiped })
    }
}


// Messages
internal sealed interface ViewingMessage

internal sealed interface EditingMessage {
    data object StartCreatingShop : EditingMessage
    data object FinishCreatingShop : EditingMessage
    data class SetErrorMessage(val error: CategoryError, val message: String?) : EditingMessage
    data class SetErrorMessages(val errors: Map<CategoryError, String>) : EditingMessage
    data class SetInitialCategory(val category: Category) : EditingMessage
    data class UpdateShowingErrors(val showErrors: Boolean) : EditingMessage
}

internal sealed interface CategoryMessage : ViewingMessage, EditingMessage {
    data class UpdateScreenState(val state: ScreenState) : CategoryMessage
    data class UpdateCategory(val category: Category) : CategoryMessage
    data class UpdateShops(val shops: Map<Shop, Set<Cashback>>) : CategoryMessage
    data class UpdateCashbacks(val cashbacks: List<Cashback>) : CategoryMessage
    data class ChangeSelectedShopIndex(val index: Int?) : CategoryMessage
    data class ChangeSelectedCashbackIndex(val index: Int?) : CategoryMessage
}


// States
internal interface CategoryState {
    val screenState: ScreenState
    val category: Category
    val shops: Map<Shop, Set<Cashback>>
    val cashbacks: List<Cashback>
    val selectedShopIndex: Int?
    val selectedCashbackIndex: Int?
}

@Immutable
internal data class CategoryViewingState(
    override val screenState: ScreenState = ScreenState.Stable,
    override val category: Category = Category(),
    override val shops: Map<Shop, Set<Cashback>> = emptyMap(),
    override val cashbacks: List<Cashback> = emptyList(),
    override val selectedShopIndex: Int? = null,
    override val selectedCashbackIndex: Int? = null
) : CategoryState

@Immutable
internal data class CategoryEditingState(
    override val screenState: ScreenState = ScreenState.Stable,
    val initialCategory: Category = Category(),
    override val category: Category = initialCategory,
    override val shops: Map<Shop, Set<Cashback>> = emptyMap(),
    override val cashbacks: List<Cashback> = emptyList(),
    val isCreatingShop: Boolean = false,
    override val selectedShopIndex: Int? = null,
    override val selectedCashbackIndex: Int? = null,
    val errors: Map<CategoryError, String> = emptyMap(),
    val showErrors: Boolean = false
) : CategoryState {
    fun isCategoryChanged() = initialCategory != category
}


internal enum class CategoryError {
    Name
}