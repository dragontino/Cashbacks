package com.cashbacks.features.home.impl.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

@Immutable
internal data class PaddingValuesBuilder(
    val start: Dp,
    val top: Dp,
    val end: Dp,
    val bottom: Dp
)

internal inline fun PaddingValues.copy(
    layoutDirection: LayoutDirection,
    block: PaddingValuesBuilder.() -> PaddingValuesBuilder
): PaddingValues {
    val builder = PaddingValuesBuilder(
        start = calculateStartPadding(layoutDirection),
        top = calculateTopPadding(),
        end = calculateEndPadding(layoutDirection),
        bottom = calculateBottomPadding()
    ).block()
    return PaddingValues(
        start = builder.start,
        top = builder.top,
        end = builder.end,
        bottom = builder.bottom
    )
}

@Immutable
internal data class SnackbarAction(
    val label: String,
    val onClick: () -> Unit
)