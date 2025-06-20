package com.cashbacks.features.home.impl.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Store
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.cashbacks.common.composables.model.AppBarItem
import com.cashbacks.common.resources.R
import kotlinx.serialization.Serializable

@Serializable
@Stable
internal sealed class HomeDestination : AppBarItem {

    @get:Composable
    abstract val screenTitle: String


    @Serializable
    data object Categories : HomeDestination() {
        override val screenTitle: String
            @Composable get() = stringResource(R.string.categories_title)

        override val tabTitle: String
            @Composable get() = stringResource(R.string.categories)

        override val selectedIcon: ImageVector
            @Composable get() = Icons.Rounded.Category

        override val unselectedIcon: ImageVector
            @Composable get() = Icons.Outlined.Category
    }

    @Serializable
    data object Shops : HomeDestination() {
        override val screenTitle: String
            @Composable get() = stringResource(R.string.shops_title)

        override val tabTitle: String
            @Composable get() = stringResource(R.string.shops)

        override val selectedIcon: ImageVector
            @Composable get() = Icons.Rounded.Store

        override val unselectedIcon: ImageVector
            @Composable get() = Icons.Outlined.Store
    }

    @Serializable
    data object Cashbacks : HomeDestination() {
        override val screenTitle: String
            @Composable get() = stringResource(R.string.cashbacks_title)

        override val tabTitle: String
            @Composable get() = stringResource(R.string.cashbacks)

        override val selectedIcon: ImageVector
            @Composable get() = ImageVector.vectorResource(R.drawable.cashback_filled)

        override val unselectedIcon: ImageVector
            @Composable get() = ImageVector.vectorResource(R.drawable.cashback_outlined)
    }

    @Serializable
    data object Cards : HomeDestination() {
        override val screenTitle: String
            @Composable get() = stringResource(R.string.bank_cards_title)

        override val tabTitle: String
            @Composable get() = stringResource(R.string.bank_cards)

        override val selectedIcon: ImageVector
            @Composable get() = Icons.Rounded.Payments

        override val unselectedIcon: ImageVector
            @Composable get() = Icons.Outlined.Payments
    }
}