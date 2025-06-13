package com.cashbacks.common.composables.model

data class Header(
    val title: String = "",
    val subtitle: String = "",
    val beautifulDesign: Boolean = false
) {
    fun isEmpty(): Boolean = title.isBlank()
}