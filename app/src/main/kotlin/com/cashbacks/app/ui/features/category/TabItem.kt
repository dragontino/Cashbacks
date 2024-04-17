package com.cashbacks.app.ui.features.category

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.rounded.Store
import androidx.compose.ui.res.painterResource
import com.cashbacks.app.R
import com.cashbacks.app.ui.navigation.AppBarIcon
import com.cashbacks.app.ui.navigation.AppBarItem
import com.cashbacks.app.ui.navigation.asAppBarIcon

internal enum class TabItem : AppBarItem {
    Shops {
        override val tabTitleRes: Int = R.string.shops
        override val selectedIcon: AppBarIcon = Icons.Rounded.Store.asAppBarIcon()
        override val unselectedIcon: AppBarIcon = Icons.Outlined.Store.asAppBarIcon()
    },

    Cashbacks {
        override val tabTitleRes: Int = R.string.cashbacks
        override val selectedIcon = AppBarIcon {
            painterResource(R.drawable.cashback_filled)
        }
        override val unselectedIcon = AppBarIcon {
            painterResource(R.drawable.cashback_outlined)
        }
    }
}