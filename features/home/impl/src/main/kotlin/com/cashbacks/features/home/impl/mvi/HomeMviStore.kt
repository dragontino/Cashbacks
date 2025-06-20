package com.cashbacks.features.home.impl.mvi

import androidx.compose.runtime.Immutable
import com.cashbacks.common.resources.AppInfo
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.features.bankcard.presentation.api.BankCardArgs
import com.cashbacks.features.cashback.presentation.api.CashbackArgs
import com.cashbacks.features.category.presentation.api.CategoryArgs
import com.cashbacks.features.home.impl.utils.SnackbarAction
import com.cashbacks.features.shop.presentation.api.ShopArgs
import kotlinx.serialization.Serializable

internal sealed interface HomeAction : CategoriesAction, ShopsAction, CashbacksAction, BankCardsAction {
    data object StartLoading : HomeAction
    data object FinishLoading : HomeAction
    data class DisplayMessage(val message: String, val action: SnackbarAction? = null) : HomeAction
}


internal sealed interface HomeLabel {
    data class DisplayMessage(val message: String, val action: SnackbarAction?) : HomeLabel
    data object NavigateToSettings : HomeLabel
    data class NavigateToCategory(val args: CategoryArgs) : HomeLabel
    data class NavigateToShop(val args: ShopArgs) : HomeLabel
    data class NavigateToCashback(val args: CashbackArgs) : HomeLabel
    data class NavigateToBankCard(val args: BankCardArgs) : HomeLabel
    data object OpenDrawer : HomeLabel
    data object CloseDrawer : HomeLabel
    data class OpenExternalFolder(val path: String) : HomeLabel
}


internal sealed interface HomeIntent {
    data object ClickButtonOpenSettings : HomeIntent
    data class ClickButtonExportData(val onSuccess: (path: String) -> Unit) : HomeIntent
    data class ShowMessage(val message: String, val action: SnackbarAction? = null) : HomeIntent
    data object ClickButtonOpenDrawer : HomeIntent
    data object ClickButtonCloseDrawer : HomeIntent
    data class NavigateToCategory(val args: CategoryArgs) : HomeIntent
    data class NavigateToShop(val args: ShopArgs) : HomeIntent
    data class NavigateToCashback(val args: CashbackArgs) : HomeIntent
    data class NavigateToBankCard(val args: BankCardArgs) : HomeIntent
    data class OpenExternalFolder(val path: String) : HomeIntent
}


internal sealed interface HomeMessage {
    data class UpdateScreenState(val state: ScreenState) : HomeMessage
}


@Serializable
@Immutable
internal data class HomeState(
    val appInfo: AppInfo,
    val screenState: ScreenState = ScreenState.Stable
)