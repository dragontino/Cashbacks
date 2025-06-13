package com.cashbacks.features.home.impl.navigation

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.cashbacks.common.navigation.FeatureApi
import com.cashbacks.common.navigation.enterScreenTransition
import com.cashbacks.common.navigation.exitScreenTransition
import com.cashbacks.features.home.api.Home
import com.cashbacks.features.home.impl.ui.HomeRoot
import com.cashbacks.features.settings.presentation.navigation.Settings

object HomeFeature : FeatureApi {

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable<Home>(
            enterTransition = { enterScreenTransition(expandFrom = Alignment.Start) },
            exitTransition = { exitScreenTransition(shrinkTowards = Alignment.Start) }
        ) {
            HomeRoot(
                modifier = modifier,
                navigateToSettings = {
                    navController.navigate(Settings) {
                        popUpTo<Home>()
                        launchSingleTop = true
                    }
                },
                navigateToCategory = { args ->
                    navController.navigate(args) {
                        popUpTo<Home>()
                        launchSingleTop = true
                    }
                },
                navigateToShop = {
                    navController.navigate(it) {
                        popUpTo<Home>()
                        launchSingleTop = true
                    }
                },
                navigateToCashback = {
                    navController.navigate(it) {
                        popUpTo<Home>()
                        launchSingleTop = true
                    }
                },
                navigateToCard = {
                    navController.navigate(it) {
                        popUpTo<Home>()
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}