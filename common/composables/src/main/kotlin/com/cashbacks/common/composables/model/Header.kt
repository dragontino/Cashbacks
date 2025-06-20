package com.cashbacks.common.composables.model

import androidx.compose.runtime.Immutable

@Immutable
data class Header(
    val title: String = "",
    val subtitle: String = "",
    val beautifulDesign: Boolean = false
) {
    fun isEmpty(): Boolean = title.isBlank()
}