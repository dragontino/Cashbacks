package com.cashbacks.app.ui.features.category

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.cashbacks.app.app.App
import com.cashbacks.app.ui.features.category.editing.CategoryEditingScreen
import com.cashbacks.app.ui.features.category.viewing.CategoryViewingScreen
import com.cashbacks.app.ui.features.home.HomeFeature
import com.cashbacks.app.ui.navigation.Feature
import com.cashbacks.app.ui.navigation.FeatureApi
import com.cashbacks.app.ui.navigation.FeatureArguments
import com.cashbacks.app.ui.navigation.enterScreenTransition
import com.cashbacks.app.ui.navigation.exitScreenTransition
import com.cashbacks.app.util.getEnum

class CategoryFeature(private val application: App) : FeatureApi {
    private object CategoryArguments : FeatureArguments {
        const val ID = "id"
        const val START_TAB = "start"
        override fun toStringArray() = arrayOf(ID, START_TAB)
    }

    private sealed class Category(type: String) : Feature<CategoryArguments>() {
        data object Viewing : Category("viewing")
        data object Editing : Category("editing")

        override val arguments = CategoryArguments

        override val baseRoute: String = buildString {
            append(ROOT)
            append(type[0].uppercase())
            append(type.substring(1..<type.length))
        }

        companion object {
            const val ROOT = "category"
        }
    }

    internal fun createDestinationRoute(isEditing: Boolean, args: CategoryArgs): String {
        val route = when {
            isEditing -> Category.Editing
            else -> Category.Viewing
        }
        return route.createUrl {
            mapOf(
                ID to args.id,
                START_TAB to args.startTab
            )
        }
    }


    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.navigation(
            route = Category.ROOT,
            startDestination = Category.Viewing.destinationRoute,
            enterTransition = {
                when (initialState.destination.route) {
                    Category.Editing.destinationRoute, Category.Viewing.destinationRoute -> {
                        fadeIn(
                            animationSpec = tween(
                                durationMillis = 400,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }

                    else -> enterScreenTransition(expandFrom = Alignment.End)
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    Category.Editing.destinationRoute, Category.Viewing.destinationRoute -> {
                        fadeOut(
                            animationSpec = tween(
                                durationMillis = 400,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }

                    else -> exitScreenTransition(shrinkTowards = Alignment.Start)
                }
            },
            popEnterTransition = {
                when (targetState.destination.route) {
                    Category.Editing.destinationRoute, Category.Viewing.destinationRoute -> {
                        fadeIn(
                            animationSpec = tween(
                                durationMillis = 400,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }

                    else -> enterScreenTransition(expandFrom = Alignment.Start)
                }
            },
            popExitTransition = {
                when (targetState.destination.route) {
                    Category.Editing.destinationRoute, Category.Viewing.destinationRoute -> {
                        fadeOut(
                            animationSpec = tween(
                                durationMillis = 400,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }

                    else -> exitScreenTransition(shrinkTowards = Alignment.End)
                }
            },
        ) {

            composable(
                route = Category.Viewing.destinationRoute,
                arguments = listOf(
                    navArgument(CategoryArguments.ID) {
                        type = NavType.LongType
                    },
                    navArgument(CategoryArguments.START_TAB) {
                        type = NavType.EnumType(CategoryTabItemType::class.java)
                    }
                )
            ) { backStackEntry ->

                val vmFactory = remember {
                    object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return application.appComponent.categoryViewingViewModel().create(
                                categoryId = backStackEntry.arguments?.getLong(CategoryArguments.ID) ?: 0
                            ) as T
                        }
                    }
                }

                CategoryViewingScreen(
                    viewModel = viewModel(factory = vmFactory),
                    startTab = backStackEntry.arguments.getEnum(
                        key = CategoryArguments.START_TAB,
                        defaultValue = CategoryTabItemType.Cashbacks
                    ),
                    navigateToCategory = {
                        val route = createDestinationRoute(args = it, isEditing = true)
                        navController.navigate(route) {
                            popUpTo(Category.Viewing.destinationRoute)
                            launchSingleTop = true
                        }
                    },
                    navigateToShop = {
                        val route =  application.appComponent.shopFeature().createDestinationRoute(it)
                        navController.navigate(route) {
                            popUpTo(Category.Viewing.destinationRoute)
                            launchSingleTop = true
                        }
                    },
                    navigateToCashback = {
                        val route = application.appComponent
                            .cashbackFeature()
                            .createDestinationRoute(it)
                        navController.navigate(route) {
                            popUpTo(Category.Viewing.destinationRoute)
                            launchSingleTop = true
                        }
                    },
                    navigateBack = navController::popBackStack
                )
            }

            composable(
                route = Category.Editing.destinationRoute,
                arguments = listOf(
                    navArgument(CategoryArguments.ID) {
                        type = NavType.LongType
                    },
                    navArgument(CategoryArguments.START_TAB) {
                        type = NavType.EnumType(CategoryTabItemType::class.java)
                    }
                )
            ) { backStackEntry ->
                val vmFactory = remember {
                    object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return application.appComponent.categoryEditingViewModel().create(
                                categoryId = backStackEntry.arguments?.getLong(CategoryArguments.ID) ?: 0
                            ) as T
                        }
                    }
                }

                CategoryEditingScreen(
                    viewModel = viewModel(factory = vmFactory),
                    startTab = backStackEntry.arguments.getEnum(
                        key = CategoryArguments.START_TAB,
                        defaultValue = CategoryTabItemType.Cashbacks
                    ),
                    navigateToCategory = {
                        val route = createDestinationRoute(args = it, isEditing = false)
                        val homeRoute = HomeFeature.Home.destinationRoute
                        navController.navigate(route) {
                            popUpTo(homeRoute)
                            launchSingleTop = true
                        }
                    },
                    navigateToShop = {
                        val route = application.appComponent
                            .shopFeature()
                            .createDestinationRoute(it)
                        navController.navigate(route) {
                            popUpTo(Category.Editing.destinationRoute)
                            launchSingleTop = true
                        }
                    },
                    navigateToCashback = {
                        val route = application.appComponent
                            .cashbackFeature()
                            .createDestinationRoute(it)
                        navController.navigate(route) {
                            popUpTo(Category.Editing.destinationRoute)
                            launchSingleTop = true
                        }
                    },
                    navigateBack = navController::popBackStack
                )
            }
        }
    }
}