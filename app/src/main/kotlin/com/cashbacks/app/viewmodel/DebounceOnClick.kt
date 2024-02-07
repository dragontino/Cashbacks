package com.cashbacks.app.viewmodel

import kotlinx.coroutines.flow.SharedFlow

typealias OnClick = () -> Unit

interface DebounceOnClick {
    val debounceOnClick: SharedFlow<OnClick>

    fun onItemClick(onClick: OnClick)
}