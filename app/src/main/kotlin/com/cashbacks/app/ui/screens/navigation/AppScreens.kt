package com.cashbacks.app.ui.screens.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Store
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.cashbacks.app.R
import java.util.LinkedList
import kotlin.reflect.KClass

internal sealed class AppScreens(
    protected val root: String,
    @StringRes val titleRes: Int?,
    val animationAlignment: Alignment.Horizontal
) {
    companion object {
        val values: List<AppScreens> by lazy {
            val queue = LinkedList<KClass<out AppScreens>>()
            queue.addAll(AppScreens::class.sealedSubclasses)
            return@lazy buildList {
                while (queue.isNotEmpty()) {
                    val c = queue.pop()
                    c.objectInstance
                        ?.let { add(it) }
                        ?: queue.addAll(c.sealedSubclasses)
                }
            }
        }
    }

    @Composable
    fun title() = titleRes
        ?.let { stringResource(it) }
        ?: ""

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
        @StringRes titleRes: Int,
        animationAlignment: Alignment.Horizontal
    ) : AppScreens(root, titleRes, animationAlignment)


    data object Categories : NavigationDrawerScreens(
        root = "categories",
        icon = Icons.Rounded.Category,
        titleRes = R.string.categories,
        animationAlignment = Alignment.CenterHorizontally
    ) {
        fun createUrl(): String = root
    }

    data object Settings : NavigationDrawerScreens(
        root = "settings",
        icon = Icons.Rounded.Settings,
        titleRes = R.string.settings,
        animationAlignment = Alignment.Start
    ) {
        fun createUrl() = root
    }

    data object BankCards : NavigationDrawerScreens(
        root = "cards",
        icon = Icons.Rounded.CreditCard,
        titleRes = R.string.bank_cards,
        animationAlignment = Alignment.Start
    ) {
        fun createUrl() = root
    }


    data object BankCardViewer : AppScreens(
        root = "cardViewer",
        titleRes = null,
        animationAlignment = Alignment.Start
    ) {
        enum class Args {
            Id
        }

        override val args = Args.entries.toStringArray()

        fun createUrl(id: Long) = "$root/$id"

    }

    data object BankCardEditor : AppScreens(
        root = "cardEditor",
        titleRes = R.string.bank_card,
        animationAlignment = Alignment.Start
    ) {
        enum class Args {
            Id
        }

        override val args: Array<String> = Args.entries.toStringArray()

        fun createUrl(id: Long?) = "$root/$id"

    }

    data object CategoryViewer : AppScreens(
        root = "categoryViewer",
        titleRes = null,
        animationAlignment = Alignment.End,
    ) {
        enum class Args {
            Id
        }

        override val args: Array<String> = Args.entries.toStringArray()
        fun createUrl(id: Long) = "$root/$id"
    }


    data object CategoryEditor : AppScreens(
        root = "categoryEditor",
        titleRes = R.string.category_title,
        animationAlignment = Alignment.End
    ) {
        enum class Args {
            Id
        }

        override val args: Array<String> = Args.entries.toStringArray()
        fun createUrl(id: Long) = "$root/$id"
    }


    sealed class TabPages(
        root: String,
        animationAlignment: Alignment.Horizontal,
        @StringRes titleRes: Int,
        @StringRes val tabTitleRes: Int,
        val icon: ImageVector,
    ) : AppScreens(root, titleRes, animationAlignment)

    data object Shop : TabPages(
        root = "shop",
        titleRes = R.string.shop,
        tabTitleRes = R.string.tab_shops,
        icon = Icons.Rounded.Store,
        animationAlignment = Alignment.End
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
        titleRes = R.string.cashback_title,
        tabTitleRes = R.string.tab_cashbacks,
        icon = Icons.Rounded.Percent,
        animationAlignment = Alignment.End
    ) {
        enum class Args {
            ParentName,
            ParentId,
            Id
        }

        override val args: Array<String> = Args.entries.toStringArray()


        fun createUrlFromCategory(
            id: Long?,
            categoryId: Long
        ) = createUrl(
            id = id,
            parentId = categoryId,
            parentName = com.cashbacks.domain.model.Category::class.simpleName!!
        )

        fun createUrlFromShop(
            id: Long?,
            shopId: Long
        ) = createUrl(
            id = id,
            parentId = shopId,
            parentName = com.cashbacks.domain.model.Shop::class.simpleName!!
        )

        private fun createUrl(
            id: Long?,
            parentId: Long,
            parentName: String
        ) = "$root/$parentName/$parentId/$id"
    }
}