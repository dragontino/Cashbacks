package com.cashbacks.features.home.impl.mvi

import androidx.compose.runtime.Immutable
import com.cashbacks.common.composables.management.DialogType
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.domain.model.FullCashback
import com.cashbacks.features.cashback.presentation.api.CashbackArgs
import com.cashbacks.features.home.impl.composables.HomeTopAppBarState
import kotlinx.serialization.Serializable

internal sealed interface CashbacksAction {
    data class LoadCashbacks(val cashbacks: List<FullCashback>?) : CashbacksAction
}


internal sealed interface CashbacksLabel {
    data class DisplayMessage(val message: String) : CashbacksLabel
    data class NavigateToCashback(val args: CashbackArgs) : CashbacksLabel
    data object OpenNavigationDrawer : CashbacksLabel
    data object NavigateBack : CashbacksLabel
    data class ChangeOpenedDialog(val type: DialogType?) : CashbacksLabel
}


internal sealed interface CashbacksIntent {
    data object ClickButtonBack : CashbacksIntent
    data object ClickNavigationButton : CashbacksIntent

    data class NavigateToCashback(val args: CashbackArgs) : CashbacksIntent

    data class SwipeCashback(val position: Int? = null) : CashbacksIntent {
        constructor(position: Int, isSwiped: Boolean) : this(position.takeIf { isSwiped })
    }

    data class DeleteCashback(val cashback: Cashback) : CashbacksIntent

    data class OpenDialog(val type: DialogType) : CashbacksIntent
    data object CloseDialog : CashbacksIntent

    data object OpenBottomSheet : CashbacksIntent
    data object CloseBottomSheet : CashbacksIntent

    data class ChangeAppBarState(val state: HomeTopAppBarState) : CashbacksIntent
}


internal sealed interface CashbacksMessage {
    data class UpdateScreenState(val state: ScreenState) : CashbacksMessage
    data class UpdateAppBarState(val state: HomeTopAppBarState) : CashbacksMessage
    data class UpdateCashbacks(val cashbacks: List<FullCashback>?) : CashbacksMessage
    data class UpdateShowingBottomSheet(val showBottomSheet: Boolean) : CashbacksMessage
    data class UpdateSelectedCashbackIndex(val index: Int?) : CashbacksMessage
}


@Serializable
@Immutable
internal data class CashbacksState(
    val screenState: ScreenState = ScreenState.Stable,
    val appBarState: HomeTopAppBarState = HomeTopAppBarState.TopBar,
    val cashbacks: List<FullCashback>? = emptyList(),
    val showBottomSheet: Boolean = false,
    val selectedCashbackIndex: Int? = null
)