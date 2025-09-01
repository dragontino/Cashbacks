package com.cashbacks.features.home.impl.mvi

import androidx.compose.runtime.Immutable
import com.cashbacks.common.composables.management.DialogType
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.common.composables.management.ViewModelState
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.presentation.api.CashbackArgs
import com.cashbacks.features.home.impl.composables.HomeTopAppBarState
import com.cashbacks.features.shop.domain.model.CategoryShop
import com.cashbacks.features.shop.domain.model.Shop
import com.cashbacks.features.shop.presentation.api.ShopArgs
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

internal sealed interface ShopsAction {
    data class LoadShops(val shops: List<ShopWithCashback>?) : ShopsAction
}


internal sealed interface ShopsLabel {
    data class DisplayMessage(val message: String) : ShopsLabel
    data class NavigateToShop(val args: ShopArgs) : ShopsLabel
    data class NavigateToCashback(val args: CashbackArgs) : ShopsLabel
    data object OpenNavigationDrawer : ShopsLabel
    data object NavigateBack : ShopsLabel
    data class OpenDialog(val type: DialogType) : ShopsLabel
    data object CloseDialog : ShopsLabel
}


internal sealed interface ShopsIntent {
    data object StartEdit : ShopsIntent
    data object FinishEdit : ShopsIntent
    data object SwitchEdit : ShopsIntent

    data object ClickButtonBack : ShopsIntent
    data object ClickNavigationButton : ShopsIntent

    data class NavigateToShop(val args: ShopArgs) : ShopsIntent
    data class NavigateToCashback(val args: CashbackArgs) : ShopsIntent

    data class DeleteShop(val shop: Shop) : ShopsIntent

    data class OpenDialog(val type: DialogType) : ShopsIntent
    data object CloseDialog : ShopsIntent
    data class ChangeAppBarState(val state: HomeTopAppBarState) : ShopsIntent

    data class SwipeShop(val id: String? = null) : ShopsIntent {
        constructor(id: String, isSwiped: Boolean) : this(id.takeIf { isSwiped })
    }

    data class SelectShop(val id: String? = null) : ShopsIntent {
        constructor(id: String, isSelected: Boolean) : this(id.takeIf { isSelected })
    }
}


internal sealed interface ShopsMessage {
    data class UpdateScreenState(val state: ScreenState) : ShopsMessage
    data class UpdateViewModelState(val state: ViewModelState) : ShopsMessage
    data class UpdateAppBarState(val state: HomeTopAppBarState) : ShopsMessage
    data class UpdateShops(val shops: ImmutableList<ShopWithCashback>?) : ShopsMessage

    data class UpdateSwipedShopId(val id: String?) : ShopsMessage
    data class UpdateSelectedShopId(val id: String?) : ShopsMessage
}


@Serializable
@Immutable
internal data class ShopsState(
    val screenState: ScreenState = ScreenState.Stable,
    val viewModelState: ViewModelState = ViewModelState.Viewing,
    val appBarState: HomeTopAppBarState = HomeTopAppBarState.TopBar,
    val shops: ImmutableList<ShopWithCashback>? = persistentListOf(),
    val swipedShopId: String? = null,
    val selectedShopId: String? = null
)


@Serializable
@Immutable
internal data class ShopWithCashback(val shop: CategoryShop, val maxCashback: Cashback?) {
    val id: String get() = when(maxCashback) {
        null -> shop.id.toString()
        else -> "${shop.id}-${maxCashback.id}"
    }
}