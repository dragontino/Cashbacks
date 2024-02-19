package com.cashbacks.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

interface AppBarItem {
    @get:StringRes
    val tabTitleRes: Int
    val selectedIcon: AppBarIcon
    val unselectedIcon: AppBarIcon
}


data class AppBarIcon(
    val imageVector: ImageVector? = null,
    val painterLambda: (@Composable () -> Painter)? = null
) {
    @Composable
    fun Icon(
        modifier: Modifier = Modifier,
        contentDescription: String? = null,
        tint: Color = LocalContentColor.current
    ) {
        val painterLambdaState = rememberUpdatedState(painterLambda)

        imageVector?.let {
            androidx.compose.material3.Icon(
                imageVector = it,
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint
            )
        } ?: painterLambdaState.value?.let {
            androidx.compose.material3.Icon(
                painter = it.invoke(),
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint
            )
        }
    }
}


fun ImageVector.asAppBarIcon() = AppBarIcon(imageVector = this)
fun AppBarIcon(painter: @Composable () -> Painter): AppBarIcon {
    return AppBarIcon(painterLambda = painter)
}