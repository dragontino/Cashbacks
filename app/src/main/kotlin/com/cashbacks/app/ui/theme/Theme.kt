package com.cashbacks.app.ui.theme

import androidx.compose.runtime.Composable
import com.cashbacks.common.composables.theme.CashbacksTheme
import com.cashbacks.features.settings.domain.model.Settings
import com.cashbacks.features.settings.presentation.utils.isDark

@Composable
fun CashbacksTheme(
    settings: Settings,
    content: @Composable () -> Unit
) {
    CashbacksTheme(
        isDarkTheme = settings.colorDesign.isDark,
        dynamicColor = settings.dynamicColor,
        content = content
    )
}