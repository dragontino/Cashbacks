package com.cashbacks.features.shop.presentation.impl.navigation

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.cashbacks.common.navigation.FeatureApi
import com.cashbacks.common.navigation.enterScreenTransition
import com.cashbacks.common.navigation.exitScreenTransition
import com.cashbacks.features.shop.presentation.api.ShopArgs
import com.cashbacks.features.shop.presentation.impl.ui.ShopRoot
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parameterSetOf

object ShopFeature : FeatureApi {
    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable<ShopArgs>(
            enterTransition = { enterScreenTransition(expandFrom = Alignment.End) },
            exitTransition = { exitScreenTransition(shrinkTowards = Alignment.Start) },
            popEnterTransition = { enterScreenTransition(expandFrom = Alignment.Start) },
            popExitTransition = { exitScreenTransition(shrinkTowards = Alignment.End) }
        ) {
            val args = it.toRoute<ShopArgs>()
            ShopRoot(
                viewModel = koinViewModel {
                    parameterSetOf(
                        args.shopId,
                        args.isEditing
                    )
                },
                navigateBack = navController::popBackStack,
                navigateToCashback = {
                    navController.navigate(it) {
                        popUpTo<ShopArgs>()
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}