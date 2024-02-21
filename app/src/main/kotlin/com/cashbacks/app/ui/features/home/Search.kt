package com.cashbacks.app.ui.features.home

import androidx.compose.runtime.State

internal interface Search {
    val query: State<String>
}