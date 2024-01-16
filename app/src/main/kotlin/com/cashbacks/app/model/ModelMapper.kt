package com.cashbacks.app.model

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.rounded.Devices
import com.cashbacks.app.R
import com.cashbacks.domain.model.ColorDesign

data object ColorDesignMapper {
    fun ColorDesign.title(context: Context): String = when (this) {
        ColorDesign.Light -> context.getString(R.string.light_theme)
        ColorDesign.Dark -> context.getString(R.string.dark_theme)
        ColorDesign.System -> context.getString(R.string.system_theme)
    }

    val ColorDesign.icon get() = when (this) {
        ColorDesign.Light -> Icons.Outlined.LightMode
        ColorDesign.Dark -> Icons.Outlined.DarkMode
        ColorDesign.System -> Icons.Rounded.Devices
    }
}