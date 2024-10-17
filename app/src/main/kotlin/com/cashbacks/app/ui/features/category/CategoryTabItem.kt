package com.cashbacks.app.ui.features.category

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.rounded.Store
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.cashbacks.app.ui.navigation.AppBarItem
import com.cashbacks.domain.R

enum class CategoryTabItemType {
    Cashbacks,
    Shops
}


internal sealed class CategoryTabItem : AppBarItem {
    abstract val type: CategoryTabItemType

    data object Shops : CategoryTabItem() {
        override val type: CategoryTabItemType = CategoryTabItemType.Shops

        override val tabTitle: String
            @Composable get() = stringResource(R.string.shops)

        override val selectedIcon: ImageVector
            @Composable get() = Icons.Rounded.Store

        override val unselectedIcon: ImageVector
            @Composable get() = Icons.Outlined.Store

    }


    data object Cashbacks : CategoryTabItem() {
        override val type: CategoryTabItemType = CategoryTabItemType.Cashbacks

        override val tabTitle: String
            @Composable get() = stringResource(R.string.cashbacks)

        override val selectedIcon: ImageVector
            @Composable get() = ImageVector.vectorResource(R.drawable.cashback_filled)

        override val unselectedIcon: ImageVector
            @Composable get() = ImageVector.vectorResource(R.drawable.cashback_outlined)

    }
}