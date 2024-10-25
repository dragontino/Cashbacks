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
import com.cashbacks.app.ui.navigation.AppBarItem
import com.cashbacks.app.ui.navigation.enterScreenTransition
import com.cashbacks.app.ui.navigation.exitScreenTransition
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.getActivity
import com.cashbacks.domain.R
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
@Composable
internal fun HomeScreen(
    appName: String,
    appVersion: String,
    navigateToSettings: () -> Unit,
    navigateToCategory: (args: CategoryArgs, isEditing: Boolean) -> Unit,
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
            listOf(
                HomeDestination.Categories,
                HomeDestination.Shops,
                HomeDestination.Cashbacks,
                HomeDestination.Cards
            ).find {  currentRoute.value == it.route } ?: HomeDestination.Categories
        }
    }

    val childContentListStates = listOf(
        HomeDestination.Categories,
        HomeDestination.Shops,
        HomeDestination.Cashbacks,
        HomeDestination.Cards
    ).associate { it.route to rememberLazyListState() }


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

    val bottomHeightPx = rememberSaveable { mutableFloatStateOf(0f) }


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
                        title = HomeDestination.Categories.screenTitle,
                        contentState = childContentListStates[route] ?: rememberLazyListState(),
                        bottomPadding = with(LocalDensity.current) {
                            bottomHeightPx.floatValue.toDp().animate()
                        },
                        openDrawer = openDrawer,
                        navigateToCategory = navigateToCategory,
                        navigateBack = { context.getActivity()?.finish() }
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
                        title = HomeDestination.Shops.screenTitle,
                        bottomPadding = with(LocalDensity.current) {
                            bottomHeightPx.floatValue.toDp().animate()
                        },
                        openDrawer = openDrawer,
                        navigateToShop = navigateToShop,
                        navigateBack = navController::popBackStack
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
                        title = HomeDestination.Cashbacks.screenTitle,
                        bottomPadding = with(LocalDensity.current) {
                            bottomHeightPx.floatValue.toDp().animate()
                        },
                        openDrawer = openDrawer,
                        navigateToCashback = navigateToCashback,
                        navigateBack = navController::popBackStack
                    )
                }

                composable(
                    route = HomeDestination.Cards.route,
                    enterTransition = { enterScreenTransition(expandFrom = Alignment.End) },
                    exitTransition = { exitScreenTransition(shrinkTowards = Alignment.End) }
                ) {
                    CardsScreen(
                        viewModel = viewModel { provideCardsViewModel() },
                        title = HomeDestination.Cards.screenTitle,
                        bottomPadding = with(LocalDensity.current) {
                            bottomHeightPx.floatValue.toDp().animate()
                        },
                        openDrawer = openDrawer,
                        navigateToCard = navigateToCard
                    )
                }
            }

            BottomBar(
                selectedDestination = currentDestination.value,
                onClickToDestination = {
                    if (it.route != navController.currentDestination?.route) {
                        navController.navigate(it.route) {
                            popUpTo(HomeDestination.Categories.route)
                            launchSingleTop = true
                        }
                    }
                },
                modifier = Modifier
                    .onGloballyPositioned {
                        bottomHeightPx.floatValue = it.size.height.toFloat()
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
                        Icon(
                            imageVector = when(it) {
                                selectedDestination -> it.selectedIcon
                                else -> it.unselectedIcon
                            },
                            contentDescription = null,
                            modifier = Modifier.height(35.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = destination.tabTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily(VerdanaFont),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary.animate(),
                    selectedTextColor = MaterialTheme.colorScheme.primary.animate(),
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = MaterialTheme.colorScheme.onBackground.animate(),
                    unselectedTextColor = MaterialTheme.colorScheme.onBackground.animate()
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}


private sealed class HomeDestination(val route: String) : AppBarItem {

    @get:Composable
    abstract val screenTitle: String


    data object Categories : HomeDestination(route = "categories") {

        override val screenTitle: String
            @Composable get() = stringResource(R.string.categories_title)

        override val tabTitle: String
            @Composable get() = stringResource(R.string.categories)

        override val selectedIcon: ImageVector
            @Composable get() = Icons.Rounded.Category

        override val unselectedIcon: ImageVector
            @Composable get() = Icons.Outlined.Category
    }

    data object Shops : HomeDestination(route = "shops") {
        override val screenTitle: String
            @Composable get() = stringResource(R.string.shops_title)

        override val tabTitle: String
            @Composable get() = stringResource(R.string.shops)

        override val selectedIcon: ImageVector
            @Composable get() = Icons.Rounded.Store

        override val unselectedIcon: ImageVector
            @Composable get() = Icons.Outlined.Store
    }

    data object Cashbacks : HomeDestination(route = "cashbacks") {
        override val screenTitle: String
            @Composable get() = stringResource(R.string.cashbacks_title)

        override val tabTitle: String
            @Composable get() = stringResource(R.string.cashbacks)

        override val selectedIcon: ImageVector
            @Composable get() = ImageVector.vectorResource(R.drawable.cashback_filled)

        override val unselectedIcon: ImageVector
            @Composable get() = ImageVector.vectorResource(R.drawable.cashback_outlined)
    }

    data object Cards : HomeDestination(route = "cards") {
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