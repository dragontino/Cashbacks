package com.cashbacks.app.ui.managment

sealed interface ScreenEvents {
    data class OpenDialog(val type: DialogType) : ScreenEvents
    data object CloseDialog : ScreenEvents
    data class ShowSnackbar(val message: String) : ScreenEvents
    data class Navigate(val route: String?) : ScreenEvents
}