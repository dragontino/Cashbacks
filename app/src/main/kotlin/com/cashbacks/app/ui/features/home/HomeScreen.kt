package com.cashbacks.app.ui.features.home

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cashbacks.domain.R
import com.cashbacks.app.ui.composables.ModalNavigationDrawerContent
import com.cashbacks.app.ui.composables.ModalSheetDefaults
import com.cashbacks.app.ui.composables.ModalSheetItems.IconTextItem
import com.cashbacks.app.ui.features.bankcard.BankCardArgs
import com.cashbacks.app.ui.features.cashback.CashbackArgs
import com.cashbacks.app.ui.features.category.CategoryArgs
import com.cashbacks.app.ui.features.home.cards.CardsScreen
import com.cashbacks.app.ui.features.home.cards.CardsViewModel
import com.cashbacks.app.ui.features.home.cashbacks.CashbacksScreen
import com.cashbacks.app.ui.features.home.cashbacks.CashbacksViewModel
import com.cashbacks.app.ui.features.home.categories.CategoriesScreen
import com.cashbacks.app.ui.features.home.categories.CategoriesViewModel
import com.cashbacks.app.ui.features.home.shops.ShopsScreen
import com.cashbacks.app.ui.features.home.shops.ShopsViewModel
import com.cashbacks.app.ui.features.shop.ShopArgs
import com.cashbacks.app.ui.navigation.AppBarIcon
import com.cashbacks.app.ui.navigation.AppBarItem
import com.cashbacks.app.ui.navigation.asAppBarIcon
import com.cashbacks.app.ui.navigation.enterScreenTransition
import com.cashbacks.app.ui.navigation.exitScreenTransition
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.getActivity
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
@Composable
internal fun HomeScreen(
    appName: String,
    appVersion: String,
    navigateToSettings: () -> Unit,
    navigateToCategory: (args: CategoryArgs) -> Unit,
    navigateToShop: (args: ShopArgs) -> Unit,
    navigateToCashback: (args: CashbackArgs) -> Unit,
    navigateToCard: (args: BankCardArgs) -> Unit,
    provideCategoriesViewModel: () -> CategoriesViewModel,
    provideShopsViewModel: () -> ShopsViewModel,
    provideCashbacksViewModel: () -> CashbacksViewModel,
    provideCardsViewModel: () -> CardsViewModel,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val navController = rememberNavController()
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = remember {
        derivedStateOf { currentBackStackEntry.value?.destination?.route }
    }
    val currentDestination = remember {
        derivedStateOf {
            HomeDestination.values
                .find { it.route == currentRoute.value }
                ?: HomeDestination.Categories
        }
    }


    val openDrawer = remember {
        fun() {
            scope.launch {
                drawerState.animateTo(
                    targetValue = DrawerValue.Open,
                    anim = ModalSheetDefaults.drawerAnimationSpec
                )
            }
        }
    }

    val closeDrawer = remember {
        fun() {
            scope.launch {
                drawerState.animateTo(
                    targetValue = DrawerValue.Closed,
                    anim = ModalSheetDefaults.drawerAnimationSpec
                )
            }
        }
    }

    val bottomPaddingDp = rememberSaveable { mutableFloatStateOf(0f) }


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalNavigationDrawerContent(appName, appVersion) {
                IconTextItem(
                    text = stringResource(R.string.home),
                    icon = Icons.Rounded.Home,
                    iconTintColor = MaterialTheme.colorScheme.primary.animate(),
                    selected = true,
                    onClick = { closeDrawer() }
                )

                HorizontalDivider(Modifier.padding(top = 8.dp, bottom = 4.dp))

                IconTextItem(
                    text = stringResource(R.string.settings),
                    icon = Icons.Rounded.Settings,
                    iconTintColor = MaterialTheme.colorScheme.primary.animate(),
                    selected = false
                ) {
                    closeDrawer()
                    navigateToSettings()
                }
            }
        },
        gesturesEnabled = drawerState.isOpen,
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .then(modifier)
                .fillMaxSize()
        ) {
            NavHost(
                navController = navController,
                startDestination = HomeDestination.Categories.route,
                modifier = Modifier
                    .zIndex(1.1f)
                    .imePadding()
                    .fillMaxSize(),
            ) {
                composable(
                    route = HomeDestination.Categories.route,
                    enterTransition = { enterScreenTransition(expandFrom = Alignment.Start) },
                    exitTransition = { exitScreenTransition(shrinkTowards = Alignment.Start) }
                ) {
                    CategoriesScreen(
                        viewModel = viewModel { provideCategoriesViewModel() },
                        title = HomeDestination.Categories.title,
                        bottomPadding = bottomPaddingDp.floatValue.dp.animate(),
                        openDrawer = openDrawer,
                        navigateToCategory = navigateToCategory,
                        popBackStack = { context.getActivity()?.finish() }
                    )
                }

                composable(
                    route = HomeDestination.Shops.route,
                    enterTransition = {
                        val expandFrom = when (initialState.destination.route) {
                            HomeDestination.Categories.route -> Alignment.End
                            else -> Alignment.Start
                        }
                        enterScreenTransition(expandFrom)
                    },
                    exitTransition = {
                        val shrinkTowards = when (targetState.destination.route) {
                            HomeDestination.Categories.route -> Alignment.End
                            else -> Alignment.Start
                        }
                        exitScreenTransition(shrinkTowards)
                    },
                ) {
                    ShopsScreen(
                        viewModel = viewModel { provideShopsViewModel() },
                        title = HomeDestination.Shops.title,
                        bottomPadding = bottomPaddingDp.floatValue.dp,
                        openDrawer = openDrawer,
                        navigateToShop = navigateToShop,
                        popBackStack = navController::popBackStack
                    )
                }

                composable(
                    route = HomeDestination.Cashbacks.route,
                    enterTransition = {
                        val expandFrom = when (initialState.destination.route) {
                            HomeDestination.Cards.route -> Alignment.Start
                            else -> Alignment.End
                        }
                        enterScreenTransition(expandFrom)
                    },
                    exitTransition = {
                        val shrinkTowards = when (targetState.destination.route) {
                            HomeDestination.Cards.route -> Alignment.Start
                            else -> Alignment.End
                        }
                        exitScreenTransition(shrinkTowards)
                    }
                ) {
                    CashbacksScreen(
                        viewModel = viewModel { provideCashbacksViewModel() },
                        title = HomeDestination.Cashbacks.title,
                        bottomPadding = bottomPaddingDp.floatValue.dp,
                        openDrawer = openDrawer,
                        navigateToCashback = navigateToCashback,
                        popBackStack = navController::popBackStack
                    )
                }

                composable(
                    route = HomeDestination.Cards.route,
                    enterTransition = { enterScreenTransition(expandFrom = Alignment.End) },
                    exitTransition = { exitScreenTransition(shrinkTowards = Alignment.End) }
                ) {
                    CardsScreen(
                        viewModel = viewModel { provideCardsViewModel() },
                        title = HomeDestination.Cards.title,
                        bottomPadding = bottomPaddingDp.floatValue.dp,
                        openDrawer = openDrawer,
                        navigateToCard = navigateToCard
                    )
                }
            }

            BottomBar(
                selectedDestination = currentDestination.value,
                onClickToDestination = {
                    navController.navigate(it.route) {
                        popUpTo(HomeDestination.Categories.route)
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .graphicsLayer {
                        bottomPaddingDp.floatValue = size.height.toDp().value
                    }
                    .zIndex(1.2f)
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )
        }
    }
}



@Composable
private fun BottomBar(
    selectedDestination: HomeDestination,
    onClickToDestination: (HomeDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.background
            .copy(alpha = .6f)
            .animate(),
        contentColor = MaterialTheme.colorScheme.onBackground.animate(),
        modifier = modifier.fillMaxWidth()
    ) {
        arrayOf(
            HomeDestination.Categories,
            HomeDestination.Shops,
            HomeDestination.Cashbacks,
            HomeDestination.Cards
        ).forEach { destination ->
            NavigationBarItem(
                selected = destination == selectedDestination,
                onClick = { onClickToDestination(destination) },
                icon = {
                    Crossfade(
                        targetState = destination,
                        label = "bottom bar icon anim",
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                    ) {
                        when (it) {
                            selectedDestination -> destination.selectedIcon.Icon(
                                modifier = Modifier.height(35.dp)
                            )

                            else -> destination.unselectedIcon.Icon(
                                modifier = Modifier.height(35.dp)
                            )
                        }
                    }
                },
                label = {
                    Text(
                        text = destination.tabText,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary.animate(),
                    selectedTextColor = MaterialTheme.colorScheme.onBackground.animate(),
                    unselectedIconColor = MaterialTheme.colorScheme.onBackground.animate(),
                    unselectedTextColor = MaterialTheme.colorScheme.onBackground.animate()
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}


private sealed class HomeDestination(
    val route: String,
    @StringRes val titleRes: Int,
    @StringRes override val tabTitleRes: Int,
    override val selectedIcon: AppBarIcon,
    override val unselectedIcon: AppBarIcon
) : AppBarItem {

    data object Categories : HomeDestination(
        route = "categories",
        titleRes = R.string.categories_title,
        tabTitleRes = R.string.categories,
        selectedIcon = Icons.Rounded.Category.asAppBarIcon(),
        unselectedIcon = Icons.Outlined.Category.asAppBarIcon()
    )

    data object Shops : HomeDestination(
        route = "shops",
        titleRes = R.string.shops_title,
        tabTitleRes = R.string.shops,
        selectedIcon = Icons.Rounded.Store.asAppBarIcon(),
        unselectedIcon = Icons.Outlined.Store.asAppBarIcon()
    )

    data object Cashbacks : HomeDestination(
        route = "cashbacks",
        titleRes = R.string.cashbacks_title,
        tabTitleRes = R.string.cashbacks,
        selectedIcon = AppBarIcon { painterResource(R.drawable.cashback_filled) },
        unselectedIcon = AppBarIcon { painterResource(R.drawable.cashback_outlined) }
    )

    data object Cards : HomeDestination(
        route = "cards",
        titleRes = R.string.bank_cards_title,
        tabTitleRes = R.string.bank_cards,
        selectedIcon = Icons.Rounded.Payments.asAppBarIcon(),
        unselectedIcon = Icons.Outlined.Payments.asAppBarIcon()
    )

    val title: String
        @Composable
        get() = stringResource(titleRes)

    val tabText: String
        @Composable
        get() = stringResource(tabTitleRes)

    companion object {
        val values by lazy { arrayOf(Categories, Shops, Cashbacks, Cards) }
    }
}