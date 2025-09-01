package com.cashbacks.features.cashback.presentation.impl.navigation

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.cashbacks.common.navigation.FeatureApi
import com.cashbacks.common.navigation.enterScreenTransition
import com.cashbacks.common.navigation.exitScreenTransition
import com.cashbacks.features.cashback.presentation.api.CashbackArgs
import com.cashbacks.features.cashback.presentation.impl.CashbackRoot
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

object CashbackFeature : FeatureApi {
    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable<CashbackArgs>(
            enterTransition = { enterScreenTransition(Alignment.End) },
            exitTransition = { exitScreenTransition(Alignment.Start) },
            popEnterTransition = { enterScreenTransition(Alignment.Start) },
            popExitTransition = { exitScreenTransition(Alignment.End) },
        ) {
            val args = it.toRoute<CashbackArgs>()
            CashbackRoot(
                viewModel = koinViewModel {
                    parametersOf(args)
                },
                navigateToShop = {
                    navController.navigate(it) {
                        popUpTo<CashbackArgs>()
                        launchSingleTop = true
                    }
                },
                navigateToCard = {
                    navController.navigate(it) {
                        popUpTo<CashbackArgs>()
                        launchSingleTop = true
                    }
                },
                navigateBack = navController::popBackStack
            )
        }
    }
}