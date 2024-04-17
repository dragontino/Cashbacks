package com.cashbacks.app.viewmodel

import kotlinx.coroutines.flow.StateFlow

typealias OnClick = () -> Unit

interface DebounceOnClick {
    val debounceOnClick: StateFlow<OnClick?>

    fun onItemClick(onClick: OnClick)
}