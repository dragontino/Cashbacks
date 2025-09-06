package com.cashbacks.features.category.presentation.impl.mvi

import androidx.compose.runtime.Immutable
import com.cashbacks.common.composables.management.DialogType
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.presentation.api.CashbackArgs
import com.cashbacks.features.category.domain.model.Category
import com.cashbacks.features.category.presentation.api.CategoryArgs
import com.cashbacks.features.category.presentation.api.CategoryTabItemType
import com.cashbacks.features.shop.domain.model.Shop
import com.cashbacks.features.shop.presentation.api.ShopArgs
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

// Actions
internal sealed interface EditingAction {
    data class LoadInitialCategory(val category: Category) : EditingAction
}

internal sealed interface CategoryAction : EditingAction {
    data object StartLoading : CategoryAction
    data object FinishLoading : CategoryAction
    data class LoadCategory(val category: Category) : CategoryAction
    data class LoadShops(val shops: List<ShopWithCashback>) : CategoryAction
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
    data class ClickToEditShop(val shopId: Long) : EditingIntent
    data object CreateCashback : EditingIntent
    data class ClickToCashback(val cashbackId: Long) : EditingIntent
    data class UpdateErrorMessage(val error: CategoryError) : EditingIntent
}

internal sealed interface CategoryIntent : ViewingIntent, EditingIntent {
    data object ClickButtonBack : CategoryIntent

    data class OpenDialog(val type: DialogType) : CategoryIntent

    data object CloseDialog : CategoryIntent

    data class SelectShop(val id: String?) : CategoryIntent {
        constructor(id: String, isSelected: Boolean) : this(id.takeIf { isSelected })
    }

    data class SwipeShop(val id: String?) : CategoryIntent {
        constructor(id: String, isSwiped: Boolean) : this(id.takeIf { isSwiped })
    }

    data class SwipeCashback(val id: Long?) : CategoryIntent {
        constructor(id: Long, isSwiped: Boolean) : this(id.takeIf { isSwiped })
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
    data class UpdateShops(val shops: ImmutableList<ShopWithCashback>) : CategoryMessage
    data class UpdateCashbacks(val cashbacks: ImmutableList<Cashback>) : CategoryMessage
    data class ChangeSelectedShopId(val id: String?) : CategoryMessage
    data class ChangeSwipedShopId(val id: String?) : CategoryMessage
    data class ChangeSwipedCashbackId(val id: Long?) : CategoryMessage
}


// States
internal interface CategoryState {
    val screenState: ScreenState
    val category: Category
    val shops: ImmutableList<ShopWithCashback>
    val cashbacks: ImmutableList<Cashback>
    val selectedShopId: String?
    val swipedShopId: String?
    val swipedCashbackId: Long?
}

@Serializable
@Immutable
internal data class CategoryViewingState(
    override val screenState: ScreenState = ScreenState.Stable,
    override val category: Category = Category(),
    override val shops: ImmutableList<ShopWithCashback> = persistentListOf(),
    override val cashbacks: ImmutableList<Cashback> = persistentListOf(),
    override val selectedShopId: String? = null,
    override val swipedShopId: String? = null,
    override val swipedCashbackId: Long? = null
) : CategoryState

@Serializable
@Immutable
internal data class CategoryEditingState(
    override val screenState: ScreenState = ScreenState.Stable,
    val initialCategory: Category = Category(),
    override val category: Category = initialCategory,
    override val shops: ImmutableList<ShopWithCashback> = persistentListOf(),
    override val cashbacks: ImmutableList<Cashback> = persistentListOf(),
    val isCreatingShop: Boolean = false,
    override val selectedShopId: String? = null,
    override val swipedShopId: String? = null,
    override val swipedCashbackId: Long? = null,
    val errors: Map<CategoryError, String> = emptyMap(),
    val showErrors: Boolean = false
) : CategoryState {
    fun isCategoryChanged() = initialCategory != category
}


@Serializable
@Immutable
internal data class ShopWithCashback(val shop: Shop, val maxCashback: Cashback?) {
    val id: String
        get() = when (maxCashback) {
            null -> shop.id.toString()
            else -> "${shop.id}-${maxCashback.id}"
        }
}


internal enum class CategoryError {
    Name
}