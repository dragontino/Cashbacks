package com.cashbacks.features.bankcard.presentation.impl.navigation

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.cashbacks.common.navigation.FeatureApi
import com.cashbacks.common.navigation.enterScreenTransition
import com.cashbacks.common.navigation.exitScreenTransition
import com.cashbacks.features.bankcard.presentation.api.BankCardArgs
import com.cashbacks.features.bankcard.presentation.impl.ui.BankCardEditingRoot
import com.cashbacks.features.bankcard.presentation.impl.ui.BankCardViewingRoot
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

object BankCardFeature : FeatureApi {

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable<BankCardArgs.Viewing>(
            enterTransition = { enterScreenTransition(expandFrom = Alignment.End) },
            exitTransition = { exitScreenTransition(shrinkTowards = Alignment.Start) },
            popEnterTransition = { enterScreenTransition(expandFrom = Alignment.Start) },
            popExitTransition = { exitScreenTransition(shrinkTowards = Alignment.End) }
        ) {
            val args = it.toRoute<BankCardArgs.Viewing>()
            BankCardViewingRoot(
                viewModel = koinViewModel {
                    parametersOf(args.cardId)
                },
                navigateToBankCard = {
                    navController.navigate(it) {
                        popUpTo<BankCardArgs.Viewing>()
                        launchSingleTop = true
                    }
                },
                navigateBack = navController::popBackStack
            )
        }

        navGraphBuilder.composable<BankCardArgs.Editing>(
            enterTransition = { enterScreenTransition(expandFrom = Alignment.End) },
            exitTransition = { exitScreenTransition(shrinkTowards = Alignment.Start) },
            popEnterTransition = { enterScreenTransition(expandFrom = Alignment.Start) },
            popExitTransition = { exitScreenTransition(shrinkTowards = Alignment.End) }
        ) {
            val args = it.toRoute<BankCardArgs.Editing>()
            BankCardEditingRoot(
                navigateBack = navController::popBackStack,
                viewModel = koinViewModel {
                    parametersOf(args.cardId)
                },
            )
        }
    }
}