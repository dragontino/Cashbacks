package com.cashbacks.app.ui.screens.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
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
import com.cashbacks.app.ui.screens.BankCardEditorScreen
import com.cashbacks.app.ui.screens.BankCardViewerScreen
import com.cashbacks.app.ui.screens.CardsScreen
import com.cashbacks.app.ui.screens.CategoriesScreen
import com.cashbacks.app.ui.screens.CategoryEditorScreen
import com.cashbacks.app.ui.screens.CategoryViewerScreen
import com.cashbacks.app.ui.screens.SettingsScreen
import com.cashbacks.app.ui.screens.ShopScreen
import com.cashbacks.app.ui.screens.SingleCashbackScreen
import com.cashbacks.app.util.AnimationDefaults
import com.cashbacks.app.util.getActivity
import com.cashbacks.app.util.mirror
import com.cashbacks.app.viewmodel.BankCardEditorViewModel
import com.cashbacks.app.viewmodel.BankCardViewerViewModel
import com.cashbacks.app.viewmodel.CardsViewModel
import com.cashbacks.app.viewmodel.CashbackViewModel
import com.cashbacks.app.viewmodel.CategoriesViewModel
import com.cashbacks.app.viewmodel.CategoryEditorViewModel
import com.cashbacks.app.viewmodel.CategoryViewerViewModel
import com.cashbacks.app.viewmodel.ShopViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun NavigationScreen(application: App, isDarkTheme: Boolean) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = remember {
        derivedStateOf { currentBackStackEntry.value?.destination?.route }
    }
    val context = LocalContext.current

    val openDrawer = remember {
        fun() {
            scope.launch {
                delay(50)
                drawerState.animateTo(
                    DrawerValue.Open,
                    tween(durationMillis = 500, easing = FastOutSlowInEasing)
                )
            }
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
            animatedComposableScreen(
                screen = AppScreens.Categories,
                enterTransition = {
                    val expandFrom = AppScreens.values
                        .find { it.destinationRoute == initialState.destination.route }
                        ?.animationAlignment?.mirror
                        ?: Alignment.Start
                    enterScreenTransition(
                        expandFrom = expandFrom,
                        animationTime = AnimationDefaults.ScreenDelayMillis - 100
                    )
                },
                exitTransition = {
                    val shrinkTowards = AppScreens.values
                        .find { it.destinationRoute == targetState.destination.route }
                        ?.animationAlignment?.mirror
                        ?: Alignment.Start
                    exitScreenTransition(
                        shrinkTowards = shrinkTowards,
                        animationTime = AnimationDefaults.ScreenDelayMillis + 100
                    )
                },
            ) {
                val vmFactory = CategoriesViewModel.Factory(
                    application = application,
                    addCategoryUseCase = application.dependencyFactory.provideAddCategoryUseCase(),
                    fetchCategoriesUseCase = application.dependencyFactory.provideFetchCategoriesUseCase(),
                    deleteCategoryUseCase = application.dependencyFactory.provideDeleteCategoryUseCase()
                )
                CategoriesScreen(
                    viewModel = viewModel(factory = vmFactory),
                    openDrawer = { openDrawer() },
                    navigateTo = navController::navigateTo,
                    popBackStack = { context.getActivity()?.finish() }
                )
            }
            
            animatedComposableScreen(
                screen = AppScreens.Settings,
                enterTransition = { enterScreenTransition(expandFrom = it.animationAlignment) },
                exitTransition = {
                    val shrinkTowards = when (targetState.destination.route) {
                        AppScreens.Categories.destinationRoute -> it.animationAlignment
                        else -> it.animationAlignment.mirror
                    }
                    exitScreenTransition(shrinkTowards)
                }
            ) {
                SettingsScreen(
                    viewModel = viewModel(factory = application.viewModelFactory),
                    isDarkTheme = isDarkTheme,
                    openDrawer = openDrawer
                )
            }

            animatedComposableScreen(
                screen = AppScreens.BankCards,
                enterTransition = { enterScreenTransition(expandFrom = it.animationAlignment) },
                exitTransition = {
                    val shrinkTowards = when (targetState.destination.route) {
                        AppScreens.Categories.destinationRoute -> it.animationAlignment
                        else -> it.animationAlignment.mirror
                    }
                    exitScreenTransition(shrinkTowards)
                },
                popEnterTransition = { enterScreenTransition(expandFrom = it.animationAlignment.mirror) }
            ) {
                val vmFactory = CardsViewModel.Factory(
                    useCase = application.dependencyFactory.provideFetchBankCardsUseCase()
                )

                CardsScreen(
                    openDrawer = openDrawer,
                    viewModel = viewModel(factory = vmFactory),
                    navigateTo = remember {
                        fun (route: String) {
                            navController.navigateTo(route, parentScreen = AppScreens.BankCards)
                        }
                    }
                )
            }

            animatedComposableScreen(
                screen = AppScreens.BankCardViewer,
                enterTransition = { enterScreenTransition(expandFrom = it.animationAlignment) },
                exitTransition = { exitScreenTransition(shrinkTowards = it.animationAlignment.mirror) },
                popEnterTransition = { enterScreenTransition(expandFrom = it.animationAlignment.mirror) },
                popExitTransition = { exitScreenTransition(shrinkTowards = it.animationAlignment) },
                arguments = listOf(
                    navArgument(AppScreens.BankCardViewer.Args.Id.name) {
                        type = NavType.LongType
                    }
                )
            ) {
                val vmFactory = BankCardViewerViewModel.Factory(
                    getBankCardUseCase = application.dependencyFactory.provideGetBankCardUseCase(),
                    deleteBankCardUseCase = application.dependencyFactory.provideDeleteBankCardUseCase(),
                    cardId = it.arguments?.getLong(AppScreens.BankCardViewer.Args.Id.name) ?: 0
                )

                BankCardViewerScreen(
                    viewModel = viewModel(
                        viewModelStoreOwner = it,
                        factory = vmFactory
                    ),
                    popBackStack = navController::popBackStack,
                    navigateTo = { route ->
                        navController.navigateTo(
                            route = route,
                            parentScreen = AppScreens.BankCardViewer,
                            saveState = false,
                            restoreState = false
                        )
                    }
                )
            }

            animatedComposableScreen(
                screen = AppScreens.BankCardEditor,
                enterTransition = { enterScreenTransition(expandFrom = it.animationAlignment) },
                exitTransition = { exitScreenTransition(shrinkTowards = it.animationAlignment) },
                arguments = listOf(
                    navArgument(AppScreens.BankCardEditor.Args.Id.name) {
                        type = NavType.StringType
                        nullable = true
                    }
                )
            ) {
                val vmFactory = BankCardEditorViewModel.Factory(
                    getBankCardUseCase = application.dependencyFactory.provideGetBankCardUseCase(),
                    editBankCardUseCase = application.dependencyFactory.provideEditBankCardUseCase(),
                    id = it.arguments?.getString(AppScreens.BankCardEditor.Args.Id.name)?.toLongOrNull()
                )

                BankCardEditorScreen(
                    viewModel = viewModel(factory = vmFactory),
                    popBackStack = navController::popBackStack
                )
            }

            animatedComposableScreen(
                screen = AppScreens.CategoryViewer,
                enterTransition = { enterScreenTransition(expandFrom = it.animationAlignment) },
                exitTransition = { exitScreenTransition(shrinkTowards = it.animationAlignment.mirror) },
                popEnterTransition = {
                    when (initialState.destination.route) {
                        AppScreens.CategoryEditor.destinationRoute -> fadeIn()
                        else -> enterScreenTransition(expandFrom = it.animationAlignment.mirror)
                    }
                },
                popExitTransition = { exitScreenTransition(shrinkTowards = it.animationAlignment) },
                arguments = listOf(
                    navArgument(AppScreens.CategoryViewer.Args.Id.name) {
                        type = NavType.LongType
                    }
                )
            ) {
                val vmFactory = CategoryViewerViewModel.Factory(
                    getCategoryUseCase = application.dependencyFactory.provideGetCategoryUseCase(),
                    deleteShopUseCase = application.dependencyFactory.provideDeleteShopUseCase(),
                    deleteCashbackUseCase = application.dependencyFactory.provideCashbackCategoryUseCase(),
                    fetchShopsFromCategoryUseCase = application.dependencyFactory.provideFetchShopsUseCase(),
                    fetchCashbacksUseCase = application.dependencyFactory.provideFetchCashbacksUseCase(),
                    categoryId = it.arguments?.getLong(AppScreens.CategoryViewer.Args.Id.name) ?: 0
                )

                CategoryViewerScreen(
                    viewModel = viewModel(factory = vmFactory),
                    navigateTo = { route ->
                        navController.navigateTo(route = route, parentScreen = AppScreens.CategoryViewer)
                    },
                    popBackStack = navController::popBackStack
                )
            }


            animatedComposableScreen(
                screen = AppScreens.CategoryEditor,
                enterTransition = { fadeIn() },
                exitTransition = { exitScreenTransition(shrinkTowards = it.animationAlignment) },
                popEnterTransition = { enterScreenTransition(expandFrom = it.animationAlignment) },
                popExitTransition = { fadeOut() },
                arguments = listOf(
                    navArgument(AppScreens.CategoryEditor.Args.Id.name) {
                        type = NavType.LongType
                    }
                )
            ) {
                val vmFactory = CategoryEditorViewModel.Factory(
                    application = application,
                    getCategoryUseCase = application.dependencyFactory.provideGetCategoryUseCase(),
                    addShopUseCase = application.dependencyFactory.provideAddShopUseCase(),
                    updateCategoryUseCase = application.dependencyFactory.provideUpdateCategoryUseCase(),
                    deleteCategoryUseCase = application.dependencyFactory.provideDeleteCategoryUseCase(),
                    fetchShopsFromCategoryUseCase = application.dependencyFactory.provideFetchShopsUseCase(),
                    fetchCashbacksUseCase = application.dependencyFactory.provideFetchCashbacksUseCase(),
                    deleteShopUseCase = application.dependencyFactory.provideDeleteShopUseCase(),
                    deleteCashbackUseCase = application.dependencyFactory.provideCashbackCategoryUseCase(),
                    categoryId = it.arguments?.getLong(AppScreens.CategoryEditor.Args.Id.name) ?: 0
                )

                CategoryEditorScreen(
                    viewModel = viewModel(factory = vmFactory),
                    navigateTo = { route ->
                        navController.navigateTo(route, parentScreen = AppScreens.CategoryEditor)
                    },
                    popBackStack = navController::popBackStack
                )
            }


            animatedComposableScreen(
                screen = AppScreens.Cashback,
                enterTransition = { enterScreenTransition(expandFrom = it.animationAlignment) },
                exitTransition = { exitScreenTransition(shrinkTowards = it.animationAlignment) },
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
                )
            ) {
                val vmFactory = CashbackViewModel.Factory(
                    cashbackCategoryUseCase = application.dependencyFactory.provideCashbackCategoryUseCase(),
                    cashbackShopUseCase = application.dependencyFactory.provideCashbackShopUseCase(),
                    editCashbackUseCase = application.dependencyFactory.provideEditCashbackUseCase(),
                    fetchBankCardsUseCase = application.dependencyFactory.provideFetchBankCardsUseCase(),
                    id = it.arguments?.getString(AppScreens.Cashback.Args.Id.name)?.toLong(),
                    parentId = it.arguments!!.getLong(AppScreens.Cashback.Args.ParentId.name),
                    parentName = it.arguments?.getString(AppScreens.Cashback.Args.ParentName.name) ?: ""
                )

                SingleCashbackScreen(
                    viewModel = viewModel(factory = vmFactory),
                    navigateTo = { route ->
                        navController.navigateTo(route, parentScreen = AppScreens.Cashback)
                    },
                    popBackStack = navController::popBackStack
                )
            }

            animatedComposableScreen(
                screen = AppScreens.Shop,
                enterTransition = { enterScreenTransition(expandFrom = it.animationAlignment) },
                exitTransition = { exitScreenTransition(shrinkTowards = it.animationAlignment.mirror) },
                popEnterTransition = { enterScreenTransition(expandFrom = it.animationAlignment.mirror) },
                popExitTransition = { exitScreenTransition(shrinkTowards = it.animationAlignment) },
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
                    application = application,
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
    parentScreen: AppScreens = AppScreens.Categories,
    saveState: Boolean = false,
    restoreState: Boolean = false
) {
    navigate(route) {
        val parent = graph.findNode(route = parentScreen.destinationRoute)
            ?: graph.findStartDestination()
        popUpTo(parent.id) { this.saveState = saveState }
        launchSingleTop = true
        this.restoreState = restoreState
    }
}



private fun NavGraphBuilder.animatedComposableScreen(
    screen: AppScreens,

    enterTransition:
    AnimatedContentTransitionScope<NavBackStackEntry>.(AppScreens) -> EnterTransition,

    exitTransition:
    AnimatedContentTransitionScope<NavBackStackEntry>.(AppScreens) -> ExitTransition,

    popEnterTransition:
    AnimatedContentTransitionScope<NavBackStackEntry>.(AppScreens) -> EnterTransition =
        enterTransition,

    popExitTransition:
    AnimatedContentTransitionScope<NavBackStackEntry>.(AppScreens) -> ExitTransition =
        exitTransition,

    arguments: List<NamedNavArgument> = listOf(),

    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = screen.destinationRoute,
        arguments = arguments,
        enterTransition = { enterTransition(screen) },
        exitTransition = { exitTransition(screen) },
        popEnterTransition = { popEnterTransition(screen) },
        popExitTransition = { popExitTransition(screen) },
        content = content
    )
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
            Alignment.End -> fullWidth
            else -> 0
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