package com.cashbacks.features.login.impl.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.cashbacks.common.composables.utils.loadingContentAnimationSpec
import com.cashbacks.common.navigation.FeatureApi
import com.cashbacks.features.login.api.LoginRoute
import com.cashbacks.features.login.impl.LoginRoot

object LoginFeature : FeatureApi {
    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable<LoginRoute>(
            enterTransition = { fadeIn(loadingContentAnimationSpec()) },
            exitTransition = { fadeOut(loadingContentAnimationSpec()) },
        ) {
            LoginRoot()
        }
    }
}