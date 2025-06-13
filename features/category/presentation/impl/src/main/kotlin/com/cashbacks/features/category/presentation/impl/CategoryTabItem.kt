package com.cashbacks.features.category.presentation.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.rounded.Store
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.cashbacks.common.composables.model.AppBarItem
import com.cashbacks.common.resources.R
import com.cashbacks.features.category.presentation.api.CategoryTabItemType

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


internal val tabItems by lazy {
    listOf(CategoryTabItem.Cashbacks, CategoryTabItem.Shops)
}