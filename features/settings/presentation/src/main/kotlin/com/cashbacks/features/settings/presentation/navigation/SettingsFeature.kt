package com.cashbacks.features.settings.presentation.navigation

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.cashbacks.common.navigation.FeatureApi
import com.cashbacks.common.navigation.enterScreenTransition
import com.cashbacks.common.navigation.exitScreenTransition
import com.cashbacks.features.settings.presentation.ui.SettingsRoot

object SettingsFeature : FeatureApi {
    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable<Settings>(
            enterTransition = { enterScreenTransition(expandFrom = Alignment.Companion.End) },
            exitTransition = { exitScreenTransition(shrinkTowards = Alignment.Companion.End) }
        ) {
            SettingsRoot(navigateBack = navController::popBackStack)
        }
    }
}