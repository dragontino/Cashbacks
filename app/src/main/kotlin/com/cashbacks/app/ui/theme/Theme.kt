package com.cashbacks.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.cashbacks.common.composables.theme.CashbacksTheme
import com.cashbacks.features.settings.domain.model.Settings
import com.cashbacks.features.settings.presentation.utils.isDark

@Composable
@Stable
fun CashbacksTheme(
    settings: Settings,
    content: @Composable (isDarkTheme: Boolean) -> Unit
) {
    val isDarkTheme = settings.colorDesign.isDark
    CashbacksTheme(
        isDarkTheme = isDarkTheme,
        dynamicColor = settings.dynamicColor,
        content = { content(isDarkTheme) }
    )
}