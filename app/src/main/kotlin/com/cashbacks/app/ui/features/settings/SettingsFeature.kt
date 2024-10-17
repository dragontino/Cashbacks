package com.cashbacks.app.ui.features.settings

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.cashbacks.app.app.App
import com.cashbacks.app.ui.navigation.Feature
import com.cashbacks.app.ui.navigation.FeatureApi
import com.cashbacks.app.ui.navigation.enterScreenTransition
import com.cashbacks.app.ui.navigation.exitScreenTransition

class SettingsFeature(private val application: App) : FeatureApi {
    object Settings : Feature {
        override val baseRoute = "settings"
    }

    fun createDestinationRoute(): String {
        return Settings.destinationRoute
    }

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(
            route = Settings.destinationRoute,
            enterTransition = { enterScreenTransition(expandFrom = Alignment.End) },
            exitTransition = { exitScreenTransition(shrinkTowards = Alignment.End) }
        ) {
            SettingsScreen(
                viewModel = viewModel { application.appComponent.settingsViewModel() },
                navigateBack = navController::popBackStack
            )
        }
    }
}