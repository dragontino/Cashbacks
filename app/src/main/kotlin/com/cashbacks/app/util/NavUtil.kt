package com.cashbacks.app.util

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.cashbacks.app.ui.navigation.FeatureApi

fun NavGraphBuilder.register(
    feature: FeatureApi,
    navController: NavHostController,
    modifier: Modifier
) {
    feature.registerGraph(
        navGraphBuilder = this,
        navController = navController,
        modifier = modifier
    )
}