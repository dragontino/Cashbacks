package com.cashbacks.features.category.presentation.impl.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.cashbacks.common.navigation.FeatureApi
import com.cashbacks.common.navigation.enterScreenTransition
import com.cashbacks.common.navigation.exitScreenTransition
import com.cashbacks.features.category.presentation.api.CategoryArgs
import com.cashbacks.features.category.presentation.impl.ui.CategoryEditingRoot
import com.cashbacks.features.category.presentation.impl.ui.CategoryViewingRoot
import com.cashbacks.features.home.api.Home
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parameterSetOf

object CategoryFeature : FeatureApi {

    private fun categoryEnterTransition() = fadeIn(
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        )
    )

    private fun categoryExitTransition() = fadeOut(
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        )
    )


    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable<CategoryArgs.Viewing>(
            enterTransition = {
                when {
                    initialState.destination.hasRoute<CategoryArgs.Editing>() -> categoryEnterTransition()
                    else -> enterScreenTransition(expandFrom = Alignment.End)
                }
            },
            exitTransition = {
                when {
                    targetState.destination.hasRoute<CategoryArgs.Editing>() -> categoryExitTransition()
                    else -> exitScreenTransition(shrinkTowards = Alignment.Start)
                }
            },
            popEnterTransition = {
                when {
                    initialState.destination.hasRoute<CategoryArgs.Editing>() -> categoryEnterTransition()
                    else -> enterScreenTransition(expandFrom = Alignment.Start)
                }
            },
            popExitTransition = {
                when {
                    targetState.destination.hasRoute<CategoryArgs.Editing>() -> categoryExitTransition()
                    else -> exitScreenTransition(shrinkTowards = Alignment.End)
                }
            },
        ) {
            val args = it.toRoute<CategoryArgs.Viewing>()
            CategoryViewingRoot(
                viewModel = koinViewModel {
                    parameterSetOf(args.id, args.startTab)
                },
                navigateToCategory = {
                    navController.navigate(it) {
                        popUpTo<CategoryArgs.Viewing>()
                        launchSingleTop = true
                    }
                },
                navigateToShop = {
                    navController.navigate(it) {
                        popUpTo<CategoryArgs.Viewing>()
                        launchSingleTop = true
                    }
                },
                navigateToCashback = {
                    navController.navigate(it) {
                        popUpTo<CategoryArgs.Viewing>()
                        launchSingleTop = true
                    }
                },
                navigateBack = navController::popBackStack
            )
        }


        navGraphBuilder.composable<CategoryArgs.Editing>(
            enterTransition = {
                when {
                    initialState.destination.hasRoute<CategoryArgs.Viewing>() -> categoryEnterTransition()
                    else -> enterScreenTransition(expandFrom = Alignment.End)
                }
            },
            exitTransition = {
                when {
                    targetState.destination.hasRoute<CategoryArgs.Viewing>() -> categoryExitTransition()
                    else -> exitScreenTransition(shrinkTowards = Alignment.Start)
                }
            },
            popEnterTransition = {
                when {
                    initialState.destination.hasRoute<CategoryArgs.Viewing>() -> categoryEnterTransition()
                    else -> enterScreenTransition(expandFrom = Alignment.Start)
                }
            },
            popExitTransition = {
                when {
                    targetState.destination.hasRoute<CategoryArgs.Viewing>() -> categoryExitTransition()
                    else -> exitScreenTransition(shrinkTowards = Alignment.End)
                }
            },
        ) {
            val args = it.toRoute<CategoryArgs.Editing>()
            CategoryEditingRoot(
                viewModel = koinViewModel {
                    parameterSetOf(args.id, args.startTab)
                },
                navigateToCategory = {
                    navController.navigate(it) {
                        popUpTo<Home>()
                        launchSingleTop = true
                    }
                },
                navigateToShop = {
                    navController.navigate(it) {
                        popUpTo<CategoryArgs.Editing>()
                        launchSingleTop = true
                    }
                },
                navigateToCashback = { args ->
                    navController.navigate(args) {
                        popUpTo<CategoryArgs.Editing>()
                        launchSingleTop = true
                    }
                },
                navigateBack = navController::popBackStack
            )
        }
    }
}