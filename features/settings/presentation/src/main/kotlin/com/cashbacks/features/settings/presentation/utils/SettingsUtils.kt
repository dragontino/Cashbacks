package com.cashbacks.features.settings.presentation.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.cashbacks.common.resources.R
import com.cashbacks.features.settings.domain.model.ColorDesign

val ColorDesign.title: String
    @Composable get() = when (this) {
        ColorDesign.Light -> stringResource(R.string.light_scheme)
        ColorDesign.Dark -> stringResource(R.string.dark_scheme)
        ColorDesign.System -> stringResource(R.string.system_scheme)
    }


val ColorDesign.isDark: Boolean
    @Composable get() = when (this) {
        ColorDesign.Light -> false
        ColorDesign.Dark -> true
        ColorDesign.System -> isSystemInDarkTheme()
    }


val ColorDesign.icon: ImageVector get() = when (this) {
    ColorDesign.Light -> Icons.Outlined.LightMode
    ColorDesign.Dark -> Icons.Outlined.DarkMode
    ColorDesign.System -> Icons.Rounded.Devices
}