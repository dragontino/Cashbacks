package com.cashbacks.common.navigation.utils

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.cashbacks.common.navigation.FeatureApi

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

