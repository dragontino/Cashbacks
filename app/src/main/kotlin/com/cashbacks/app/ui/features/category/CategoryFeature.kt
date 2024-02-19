package com.cashbacks.app.ui.features.category

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.rounded.Store
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.cashbacks.app.R
import com.cashbacks.app.app.App
import com.cashbacks.app.ui.features.category.editing.CategoryEditingScreen
import com.cashbacks.app.ui.features.category.viewing.CategoryViewingScreen
import com.cashbacks.app.ui.features.home.HomeFeature
import com.cashbacks.app.ui.navigation.AppBarIcon
import com.cashbacks.app.ui.navigation.AppBarItem
import com.cashbacks.app.ui.navigation.Feature
import com.cashbacks.app.ui.navigation.FeatureApi
import com.cashbacks.app.ui.navigation.asAppBarIcon
import com.cashbacks.app.ui.navigation.enterScreenTransition
import com.cashbacks.app.ui.navigation.exitScreenTransition

class CategoryFeature(private val application: App) : FeatureApi {
    private sealed class Category(type: String) : Feature {
        data object Viewing : Category("viewing")
        data object Editing : Category("editing")

        override val args = Args

        override val baseRoute: String = buildString {
            append(Root)
            append(type[0].uppercase())
            append(type.substring(1..<type.length))
        }

        fun createUrl(id: Long): String {
            return "$baseRoute/$id"
        }

        object Args : Feature.Args {
            const val Id = "id"
            override fun toStringArray() = arrayOf(Id)
        }

        companion object {
            const val Root = "category"
        }
    }

    fun createDestinationRoute(categoryArgs: CategoryArgs): String {
        val route = when {
            categoryArgs.isEditing -> Category.Editing
            else -> Category.Viewing
        }
        return route.createUrl(categoryArgs.id)
    }


    internal sealed class TabItem : AppBarItem {
        data object Shops : TabItem() {
            override val tabTitleRes = R.string.shops
            override val selectedIcon: AppBarIcon = Icons.Rounded.Store.asAppBarIcon()
            override val unselectedIcon: AppBarIcon = Icons.Outlined.Store.asAppBarIcon()
        }

        data object Cashbacks : TabItem() {
            override val tabTitleRes: Int = R.string.cashbacks
            override val selectedIcon = AppBarIcon {
                painterResource(R.drawable.cashback_filled)
            }
            override val unselectedIcon = AppBarIcon {
                painterResource(R.drawable.cashback_outlined)
            }
        }
    }


    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.navigation(
            route = Category.Root,
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
                    navArgument(Category.Args.Id) {
                        type = NavType.LongType
                    }
                )
            ) {

                val vmFactory = remember {
                    object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return application.appComponent.categoryViewerViewModel().create(
                                categoryId = it.arguments?.getLong(Category.Args.Id) ?: 0
                            ) as T
                        }
                    }
                }

                CategoryViewingScreen(
                    viewModel = viewModel(factory = vmFactory),
                    tabItems = listOf(TabItem.Shops, TabItem.Cashbacks),
                    navigateToCategory = {
                        val route = createDestinationRoute(it)
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
                    popBackStack = navController::popBackStack
                )
            }

            composable(
                route = Category.Editing.destinationRoute,
                arguments = listOf(
                    navArgument(Category.Args.Id) {
                        type = NavType.LongType
                    }
                )
            ) {
                val vmFactory = remember {
                    object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return application.appComponent.categoryEditorViewModel().create(
                                categoryId = it.arguments?.getLong(Category.Args.Id) ?: 0
                            ) as T
                        }
                    }
                }

                CategoryEditingScreen(
                    viewModel = viewModel(factory = vmFactory),
                    tabItems = listOf(TabItem.Shops, TabItem.Cashbacks),
                    navigateToCategory = {
                        val route = createDestinationRoute(it)
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
                    popBackStack = navController::popBackStack
                )
            }
        }
    }
}