package com.cashbacks.app.ui.screens.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cashbacks.app.app.App
import com.cashbacks.app.ui.composables.ModalNavigationDrawerContent
import com.cashbacks.app.ui.composables.ModalSheetItems.ScreenTypeItem
import com.cashbacks.app.ui.screens.CardsScreen
import com.cashbacks.app.ui.screens.CategoriesScreen
import com.cashbacks.app.ui.screens.CategoryInfoScreen
import com.cashbacks.app.ui.screens.SettingsScreen
import com.cashbacks.app.ui.screens.ShopScreen
import com.cashbacks.app.ui.screens.SingleCashbackScreen
import com.cashbacks.app.util.AnimationDefaults
import com.cashbacks.app.viewmodel.CashbackViewModel
import com.cashbacks.app.viewmodel.CategoriesViewModel
import com.cashbacks.app.viewmodel.CategoryInfoViewModel
import com.cashbacks.app.viewmodel.MainViewModel
import com.cashbacks.app.viewmodel.ShopViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun NavigationScreen(
    application: App,
    isDarkTheme: Boolean,
    viewModel: MainViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = remember {
        derivedStateOf {
            currentBackStackEntry?.destination?.route
        }
    }

    val openDrawer = {
        scope.launch {
            delay(50)
            drawerState.animateTo(DrawerValue.Open, tween(durationMillis = 500, easing = FastOutSlowInEasing))
        }
    }

    fun onDrawerItemClick(route: String) {
        scope.launch {
            drawerState.animateTo(
                targetValue = DrawerValue.Closed,
                anim = tween(durationMillis = 200, easing = LinearOutSlowInEasing)
            )
            navController.navigateTo(route)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalNavigationDrawerContent {
                ScreenTypeItem(
                    screen = AppScreens.Categories,
                    selected = currentRoute.value == AppScreens.Categories.destinationRoute
                ) { screen ->
                    onDrawerItemClick(route = screen.createUrl())
                }

                ScreenTypeItem(
                    screen = AppScreens.BankCards,
                    selected = currentRoute.value == AppScreens.BankCards.destinationRoute
                ) { screen ->
                    onDrawerItemClick(route = screen.createUrl())
                }

                HorizontalDivider(Modifier.padding(top = 8.dp, bottom = 4.dp))

                ScreenTypeItem(
                    screen = AppScreens.Settings,
                    selected = currentRoute.value == AppScreens.Settings.destinationRoute
                ) { screen ->
                    onDrawerItemClick(route = screen.createUrl())
                }
            }
        },
        gesturesEnabled = drawerState.isOpen
    ) {
        NavHost(
            navController = navController,
            startDestination = AppScreens.Categories.destinationRoute,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
        ) {
            composable(
                route = AppScreens.Categories.destinationRoute,
                enterTransition = {
                    val expandFrom = when (initialState.destination.route) {
                        AppScreens.Settings.destinationRoute,
                        AppScreens.BankCards.destinationRoute,
                        AppScreens.BankCard.destinationRoute -> {
                            Alignment.End
                        }
                        else -> Alignment.Start
                    }
                    enterScreenTransition(
                        expandFrom = expandFrom,
                        animationTime = AnimationDefaults.ScreenDelayMillis - 100
                    )
                },
                exitTransition = {
                    val shrinkTowards = when (targetState.destination.route) {
                        AppScreens.Settings.destinationRoute, AppScreens.BankCards.destinationRoute -> {
                            Alignment.End
                        }
                        else -> Alignment.Start
                    }
                    exitScreenTransition(
                        shrinkTowards = shrinkTowards,
                        animationTime = AnimationDefaults.ScreenDelayMillis + 100
                    )
                },
            ) {
                val vmFactory = CategoriesViewModel.Factory(
                    addCategoryUseCase = application.dependencyFactory.provideAddCategoryUseCase(),
                    fetchCategoriesUseCase = application.dependencyFactory.provideFetchCategoriesUseCase(),
                    deleteCategoryUseCase = application.dependencyFactory.provideDeleteCategoryUseCase()
                )
                CategoriesScreen(
                    viewModel = viewModel(factory = vmFactory),
                    openDrawer = { openDrawer() },
                    navigateTo = navController::navigateTo
                )
            }
            
            composable(
                route = AppScreens.Settings.destinationRoute,
                enterTransition = { enterScreenTransition(expandFrom = Alignment.Start) },
                exitTransition = { exitScreenTransition(shrinkTowards = Alignment.Start) }
            ) {
                SettingsScreen(
                    viewModel = viewModel(factory = application.viewModelFactory),
                    isDarkTheme = isDarkTheme,
                    openDrawer = { openDrawer() }
                )
            }

            composable(
                route = AppScreens.BankCards.destinationRoute,
                enterTransition = { enterScreenTransition(expandFrom = Alignment.Start) },
                exitTransition = { exitScreenTransition(shrinkTowards = Alignment.Start) }
            ) {
                CardsScreen(openDrawer = { openDrawer() })
            }

            /*composable(route = AppScreens.BankCard.destinationRoute) {
                BankCardEditorScreen(
                    viewModel = viewModel(factory = application.viewModelFactory)
                )
            }*/

            composable(
                route = AppScreens.Category.destinationRoute,
                enterTransition = { enterScreenTransition(expandFrom = Alignment.End) },
                exitTransition = { exitScreenTransition(shrinkTowards = Alignment.End) },
                arguments = listOf(
                    navArgument(AppScreens.Category.Args.Id.name) {
                        type = NavType.LongType
                    },
                    navArgument(AppScreens.Category.Args.IsEdit.name) {
                        type = NavType.BoolType
                    }
                )
            ) {
                val vmFactory = CategoryInfoViewModel.Factory(
                    editCategoryUseCase = application.dependencyFactory.provideEditCategoryUseCase(),
                    addShopUseCase = application.dependencyFactory.provideAddShopUseCase(),
                    deleteCategoryUseCase = application.dependencyFactory.provideDeleteCategoryUseCase(),
                    deleteShopUseCase = application.dependencyFactory.provideDeleteShopUseCase(),
                    deleteCashbackUseCase = application.dependencyFactory.provideCashbackCategoryUseCase(),
                    fetchShopsUseCase = application.dependencyFactory.provideFetchShopsUseCase(),
                    fetchCashbacksUseCase = application.dependencyFactory.provideFetchCashbacksUseCase(),
                    categoryId = it.arguments?.getLong(AppScreens.Category.Args.Id.name) ?: 1,
                    isEditing = it.arguments?.getBoolean(AppScreens.Category.Args.IsEdit.name) ?: false
                )

                CategoryInfoScreen(
                    viewModel = viewModel(factory = vmFactory),
                    navigateTo = { route ->
                        navController.navigateTo(route = route, parentScreen = AppScreens.Category)
                    },
                    popBackStack = navController::popBackStack
                )
            }


            composable(
                route = AppScreens.Cashback.destinationRoute,
                enterTransition = { enterScreenTransition(expandFrom = Alignment.End) },
                exitTransition = { exitScreenTransition(shrinkTowards = Alignment.End) },
                arguments = listOf(
                    navArgument(AppScreens.Cashback.Args.Id.name) {
                        type = NavType.StringType
                        nullable = true
                    },
                    navArgument(AppScreens.Cashback.Args.ParentId.name) {
                        type = NavType.LongType
                    },
                    navArgument(AppScreens.Cashback.Args.ParentName.name) {
                        type = NavType.StringType
                    },
                    navArgument(AppScreens.Cashback.Args.IsEdit.name) {
                        type = NavType.BoolType
                    }
                )
            ) {
                val vmFactory = CashbackViewModel.Factory(
                    cashbackCategoryUseCase = application.dependencyFactory.provideCashbackCategoryUseCase(),
                    cashbackShopUseCase = application.dependencyFactory.provideCashbackShopUseCase(),
                    editCashbackUseCase = application.dependencyFactory.provideEditCashbackUseCase(),
                    id = it.arguments?.getString(AppScreens.Cashback.Args.Id.name)?.toLong(),
                    parentId = it.arguments!!.getLong(AppScreens.Cashback.Args.ParentId.name),
                    parentName = it.arguments?.getString(AppScreens.Cashback.Args.ParentName.name) ?: "",
                    isEdit = it.arguments?.getBoolean(AppScreens.Cashback.Args.IsEdit.name) ?: false
                )

                SingleCashbackScreen(
                    viewModel = viewModel(factory = vmFactory),
                    navigateTo = { route ->
                        navController.navigateTo(route, parentScreen = AppScreens.Cashback)
                    },
                    popBackStack = navController::popBackStack
                )
            }

            composable(
                route = AppScreens.Shop.destinationRoute,
                enterTransition = { enterScreenTransition(expandFrom = Alignment.End) },
                exitTransition = { exitScreenTransition(shrinkTowards = Alignment.End) },
                arguments = listOf(
                    navArgument(AppScreens.Shop.Args.CategoryId.name) {
                        type = NavType.LongType
                    },
                    navArgument(AppScreens.Shop.Args.ShopId.name) {
                        type = NavType.LongType
                    },
                    navArgument(AppScreens.Shop.Args.IsEdit.name) {
                        type = NavType.BoolType
                    }
                )
            ) {
                val vmFactory = ShopViewModel.Factory(
                    fetchCashbacksUseCase = application.dependencyFactory.provideFetchCashbacksUseCase(),
                    editShopUseCase = application.dependencyFactory.provideShopUseCase(),
                    deleteShopUseCase = application.dependencyFactory.provideDeleteShopUseCase(),
                    deleteCashbackUseCase = application.dependencyFactory.provideCashbackShopUseCase(),
                    categoryId = it.arguments?.getLong(AppScreens.Shop.Args.CategoryId.name) ?: 0,
                    shopId = it.arguments?.getLong(AppScreens.Shop.Args.ShopId.name) ?: 0,
                    isEditing = it.arguments?.getBoolean(AppScreens.Shop.Args.IsEdit.name) ?: false
                )

                ShopScreen(
                    viewModel = viewModel(factory = vmFactory),
                    popBackStack = navController::popBackStack,
                    navigateTo = { route ->
                        navController.navigateTo(route = route, parentScreen = AppScreens.Shop)
                    }
                )
            }
        }
    }
}


private fun NavHostController.navigateTo(
    route: String,
    parentScreen: AppScreens? = AppScreens.Categories
) {
    navigate(route) {
        when (parentScreen) {
            null -> popUpTo(graph.findStartDestination().id) {
                inclusive = true
            }
            else -> popUpTo(parentScreen.destinationRoute)
        }
        launchSingleTop = true
    }
}


private fun enterScreenTransition(
    expandFrom: Alignment.Horizontal,
    animationTime: Int = AnimationDefaults.ScreenDelayMillis,
    delayTimePercent: Float = .05f,
): EnterTransition {
    val delayMillis = (animationTime * delayTimePercent).roundToInt()
    val durationMillis = animationTime - delayMillis
    return slideInHorizontally(
        animationSpec = tween(durationMillis, delayMillis, easing = FastOutSlowInEasing)
    ) { fullWidth ->
        when (expandFrom) {
            Alignment.Start -> -fullWidth
            else -> fullWidth
        }
    }
}


private fun exitScreenTransition(
    shrinkTowards: Alignment.Horizontal,
    animationTime: Int = AnimationDefaults.ScreenDelayMillis,
    delayTimePercent: Float = .2f
): ExitTransition {
    val delayMillis = (animationTime * delayTimePercent).roundToInt()
    val durationMillis = animationTime - delayMillis
    return slideOutHorizontally(
        animationSpec = tween(durationMillis, delayMillis, easing = FastOutSlowInEasing),
    ) { fullWidth ->
        when (shrinkTowards) {
            Alignment.Start -> -fullWidth
            else -> fullWidth
        }
    }
}