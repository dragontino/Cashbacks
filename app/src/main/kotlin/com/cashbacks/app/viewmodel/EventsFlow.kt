package com.cashbacks.app.viewmodel

import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ScreenEvents
import kotlinx.coroutines.flow.SharedFlow

interface EventsFlow {
    val eventsFlow: SharedFlow<ScreenEvents>

    fun openDialog(type: DialogType)

    fun closeDialog()

    fun navigateTo(route: String?)

    fun showSnackbar(message: String)
}