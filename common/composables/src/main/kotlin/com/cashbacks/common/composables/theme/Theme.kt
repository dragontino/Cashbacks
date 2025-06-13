package com.cashbacks.common.composables.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

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
    surface = Color(red = 34, green = 28, blue = 25, alpha = 255),
    onSurface = Color.White,
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
    surface = Color(red = 248, green = 228, blue = 221),
    onSurface = Color.Black,
    error = Color(0xFFB00909)
)


@Composable
fun CashbacksTheme(
    isDarkTheme: Boolean = false,
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