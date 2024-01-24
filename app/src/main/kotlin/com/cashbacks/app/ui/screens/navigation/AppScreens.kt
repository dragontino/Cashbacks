package com.cashbacks.app.ui.screens.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Store
import androidx.compose.ui.graphics.vector.ImageVector
import com.cashbacks.app.R

sealed class AppScreens(
    protected val root: String,
    @StringRes val titleRes: Int
) {
    protected fun <T : Enum<*>> Collection<T>.toStringArray(): Array<String> =
        map { it.name }.toTypedArray()

    protected open val args: Array<String> = emptyArray()

    val destinationRoute get() = buildString {
        append(root)
        if (args.isNotEmpty()) {
            append(
                "/",
                args.joinToString(separator = "/") { "{$it}" }
            )
        }
    }


    sealed class NavigationDrawerScreens(
        root: String,
        val icon: ImageVector,
        @StringRes titleRes: Int
    ) : AppScreens(root, titleRes)


    data object Categories : NavigationDrawerScreens(
        root = "categories",
        icon = Icons.Rounded.Category,
        titleRes = R.string.categories
    ) {
        fun createUrl(): String = root
    }

    data object Settings : NavigationDrawerScreens(
        root = "settings",
        icon = Icons.Rounded.Settings,
        titleRes = R.string.settings
    ) {
        fun createUrl() = root
    }

    data object BankCards : NavigationDrawerScreens(
        root = "cards",
        icon = Icons.Rounded.CreditCard,
        titleRes = R.string.bank_cards
    ) {
        fun createUrl() = root
    }


    data object Category : AppScreens(
        root = "category",
        titleRes = R.string.category_title
    ) {
        enum class Args {
            Id,
            IsEdit
        }

        override val args: Array<String> = Args.entries.toStringArray()
        fun createUrl(id: Long, isEdit: Boolean = false) = "$root/$id/$isEdit"
    }

    data object BankCard : AppScreens(root = "card", titleRes = R.string.bank_card) {
        enum class Args {
            Id
        }

        override val args: Array<String> = Args.entries.toStringArray()

        fun createUrl(id: Long) = "$root/$id"
    }


    sealed class TabPages(
        root: String,
        @StringRes titleRes: Int,
        @StringRes val tabTitleRes: Int,
        val icon: ImageVector,
    ) : AppScreens(root, titleRes)

    data object Shop : TabPages(
        root = "shop",
        titleRes = R.string.shop,
        tabTitleRes = R.string.tab_shops,
        icon = Icons.Rounded.Store
    ) {
        enum class Args {
            CategoryId,
            ShopId,
            IsEdit
        }

        override val args: Array<String> = Args.entries.toStringArray()

        fun createUrl(categoryId: Long, shopId: Long, isEdit: Boolean = false) =
            "$root/$categoryId/$shopId/$isEdit"
    }

    data object Cashback : TabPages(
        root = "cashback",
        titleRes = R.string.cashback,
        tabTitleRes = R.string.tab_cashbacks,
        icon = Icons.Rounded.Percent
    ) {
        enum class Args {
            ParentName,
            ParentId,
            Id,
            IsEdit
        }

        override val args: Array<String> = Args.entries.toStringArray()


        fun createUrlFromCategory(
            id: Long?,
            categoryId: Long,
            isEdit: Boolean = false
        ) = createUrl(
            id = id,
            parentId = categoryId,
            parentName = com.cashbacks.domain.model.Category::class.simpleName!!,
            isEdit = isEdit
        )

        fun createUrlFromShop(
            id: Long?,
            shopId: Long,
            isEdit: Boolean = false
        ) = createUrl(
            id = id,
            parentId = shopId,
            parentName = com.cashbacks.domain.model.Shop::class.simpleName!!,
            isEdit = isEdit
        )

        private fun createUrl(
            id: Long?,
            parentId: Long,
            parentName: String,
            isEdit: Boolean = false
        ) = "$root/$parentName/$parentId/$id/$isEdit"
    }
}