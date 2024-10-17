package com.cashbacks.app.ui.features.home

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.cashbacks.app.app.App
import com.cashbacks.app.ui.navigation.Feature
import com.cashbacks.app.ui.navigation.FeatureApi
import com.cashbacks.app.ui.navigation.enterScreenTransition
import com.cashbacks.app.ui.navigation.exitScreenTransition

class HomeFeature(private val application: App) : FeatureApi {
    object Home : Feature {
        override val baseRoute = "home"
    }

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(
            route = Home.destinationRoute,
            enterTransition = { enterScreenTransition(expandFrom = Alignment.Start) },
            exitTransition = { exitScreenTransition(shrinkTowards = Alignment.Start) }
        ) {
            HomeScreen(
                appName = application.name,
                appVersion = application.version,
                navigateToSettings = {
                    val route = application.appComponent.settingsFeature().createDestinationRoute()
                    navController.navigate(route) {
                        popUpTo(Home.destinationRoute)
                        launchSingleTop = true
                    }
                },
                navigateToCategory = { args, isEditing ->
                    val route = application.appComponent
                        .categoryFeature()
                        .createDestinationRoute(it)
                    navController.navigate(route) {
                        popUpTo(Home.destinationRoute)
                        launchSingleTop = true
                    }
                },
                navigateToShop = {
                    val route = application.appComponent.shopFeature().createDestinationRoute(it)
                    navController.navigate(route) {
                        popUpTo(Home.destinationRoute)
                        launchSingleTop = true
                    }
                },
                navigateToCashback = {
                    val route = application.appComponent
                        .cashbackFeature()
                        .createDestinationRoute(it)
                    navController.navigate(route) {
                        popUpTo(Home.destinationRoute)
                        launchSingleTop = true
                    }
                },
                navigateToCard = {
                    val route = application.appComponent
                        .bankCardFeature()
                        .createDestinationRoute(it)
                    navController.navigate(route) {
                        popUpTo(Home.destinationRoute)
                        launchSingleTop = true
                    }
                },
                provideCategoriesViewModel = application.appComponent::categoriesViewModel,
                provideShopsViewModel = application.appComponent::shopsViewModel,
                // TODO: 17.02.2024 доделать
                provideCashbacksViewModel = application.appComponent::cashbacksViewModel,
                provideCardsViewModel = application.appComponent::cardsViewModel
            )
        }
    }
}
