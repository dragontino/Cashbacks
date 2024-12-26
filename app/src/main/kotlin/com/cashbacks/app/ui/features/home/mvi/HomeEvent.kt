package com.cashbacks.app.ui.features.home.mvi

import com.cashbacks.app.mvi.MviEvent

sealed class HomeEvent : MviEvent {
    data class ShowSnackbar(val message: String) : HomeEvent()

    data object NavigateToSettings : HomeEvent()

    data object OpenDrawer : HomeEvent()

    data object CloseDrawer : HomeEvent()
}
