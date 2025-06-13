package com.cashbacks.common.composables.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

interface AppBarItem {
    @get:Composable
    val tabTitle: String

    @get:Composable
    val selectedIcon: ImageVector

    @get:Composable
    val unselectedIcon: ImageVector
}