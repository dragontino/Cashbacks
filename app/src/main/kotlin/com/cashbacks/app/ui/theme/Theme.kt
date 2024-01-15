package com.cashbacks.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.cashbacks.domain.model.ColorDesign
import com.cashbacks.domain.model.Settings

private val DarkColorScheme = darkColorScheme(
    primary = OrangeDark,
    onPrimary = Color.White,
    secondary = Color(0xFFFBF7F4),
    onSecondary = OrangeDark,
    primaryContainer = Color(0xFF6B0900),
    onPrimaryContainer = Color(0xFFFBF7F4),
    tertiary = CheckedThumbDark,
    tertiaryContainer = UncheckedThumbDark,
    background = BackgroundDark,
    onBackground = Color.White,
    secondaryContainer = Color(0xFF39393C),
    error = Color(0xFFD93232)
)

private val LightColorScheme = lightColorScheme(
    primary = OrangeLight,
    onPrimary = Color.Black,
    secondary = Color(0xFFFBF7F4),
    onSecondary = OrangeLight,
    primaryContainer = Color(0xFFBE1C1E),
    onPrimaryContainer = Color(0xFFFBF7F4),
    tertiary = CheckedThumbLight,
    tertiaryContainer = UncheckedThumbLight,
    background = Color.White,
    onBackground = Color.Black,
    secondaryContainer = Color(0xFFEDEEF3),
    error = Color(0xFFB00909)
)

@Composable
fun CashbacksTheme(
    settings: Settings,
    content: @Composable (isDarkTheme: Boolean) -> Unit
) {
    val isDarkTheme = when (settings.colorDesign) {
        ColorDesign.Light -> false
        ColorDesign.Dark -> true
        ColorDesign.System -> isSystemInDarkTheme()
    }

    CashbacksTheme(
        isDarkTheme = isDarkTheme,
        dynamicColor = settings.dynamicColor,
        content = { content(isDarkTheme) }
    )
}


@Composable
fun CashbacksTheme(
    isDarkTheme: Boolean,
    dynamicColor: Boolean = false,
    content: @Composable (() -> Unit)
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            when {
                isDarkTheme -> dynamicDarkColorScheme(context)
                else -> dynamicLightColorScheme(context)
            }.run {
                copy(surfaceTint = surface)
            }
        }

        isDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}