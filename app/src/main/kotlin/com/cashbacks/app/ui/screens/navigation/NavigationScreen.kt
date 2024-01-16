package com.cashbacks.app.ui.screens.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.cashbacks.app.ui.screens.SingleCashbackScreen
import com.cashbacks.app.viewmodel.CategoriesViewModel
import com.cashbacks.app.viewmodel.CategoryInfoViewModel
import com.cashbacks.app.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
                DrawerValue.Closed,
                tween(durationMillis = 200, easing = LinearOutSlowInEasing)
            )
            navController.navigate(route)
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
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(
                        durationMillis = 300,
                        delayMillis = 100,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                )
            },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
        ) {
            composable(
                route = AppScreens.Categories.destinationRoute,
                enterTransition = {
                    fadeIn(animationSpec = tween(durationMillis = 400, delayMillis = 200, easing = FastOutSlowInEasing))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing))
                }
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
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(
                            durationMillis = 300,
                            delayMillis = 100,
                            easing = FastOutSlowInEasing
                        )
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                    )
                }
            ) {
                SettingsScreen(
                    viewModel = viewModel(factory = application.viewModelFactory),
                    isDarkTheme = isDarkTheme,
                    openDrawer = { openDrawer() }
                )
            }

            composable(route = AppScreens.BankCards.destinationRoute) {
                CardsScreen(openDrawer = { openDrawer() })
            }

            /*composable(route = AppScreens.BankCard.destinationRoute) {
                BankCardEditorScreen(
                    viewModel = viewModel(factory = application.viewModelFactory)
                )
            }*/

            composable(
                route = AppScreens.Category.destinationRoute,
                arguments = listOf(
                    navArgument(AppScreens.Category.Args.Id.name) {
                        type = NavType.LongType
                    }
                )
            ) {
                val vmFactory = CategoryInfoViewModel.Factory(
                    categoryUseCase = application.dependencyFactory.provideEditCategoryUseCase(),
                    deleteCategoryUseCase = application.dependencyFactory.provideDeleteCategoryUseCase(),
                    shopUseCase = application.dependencyFactory.provideShopUseCase(),
                    cashbackUseCase = application.dependencyFactory.provideCashbackCategoryUseCase(),
                    id = it.arguments?.getLong(AppScreens.Category.Args.Id.name) ?: 1,
                    isEditing = false
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
                arguments = listOf(
                    navArgument(AppScreens.Cashback.Args.Id.name) {
                        type = NavType.StringType
                        nullable = true
                    }
                )
            ) {
                SingleCashbackScreen()
            }
        }
    }
}


fun NavHostController.navigateTo(
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