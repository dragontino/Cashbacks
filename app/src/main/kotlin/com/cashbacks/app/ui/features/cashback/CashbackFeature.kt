package com.cashbacks.app.ui.features.cashback

import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cashbacks.app.app.App
import com.cashbacks.app.ui.navigation.Feature
import com.cashbacks.app.ui.navigation.FeatureApi
import com.cashbacks.app.ui.navigation.enterScreenTransition
import com.cashbacks.app.ui.navigation.exitScreenTransition

class CashbackFeature(private val application: App) : FeatureApi {
    private object Cashback : Feature {
        object Args : Feature.Args {
            const val ParentName = "parentName"
            const val ParentId = "parentId"
            const val CashbackId = "cashbackId"

            override fun toStringArray() = arrayOf(ParentName, ParentId, CashbackId)
        }


        override val baseRoute = "cashback"
        override val args = Args
    }

    fun createDestinationRoute(args: CashbackArgs): String {
        return "${Cashback.baseRoute}/${args.parentName}/${args.parentId}/${args.cashbackId}"
    }

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(
            route = Cashback.destinationRoute,
            enterTransition = { enterScreenTransition(Alignment.End) },
            exitTransition = { exitScreenTransition(Alignment.Start) },
            popEnterTransition = { enterScreenTransition(Alignment.Start) },
            popExitTransition = { exitScreenTransition(Alignment.End) },
            arguments = listOf(
                navArgument(Cashback.Args.CashbackId) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(Cashback.Args.ParentId) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(Cashback.Args.ParentName) {
                    type = NavType.StringType
                    nullable = true
                },
            )
        ) {
            val vmFactory = remember {
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return application.appComponent.cashbackViewModel().create(
                            id = it.arguments?.getString(Cashback.Args.CashbackId)?.toLong(),
                            parentId = it.arguments?.getString(Cashback.Args.ParentId)?.toLong(),
                            parentName = it.arguments?.getString(Cashback.Args.ParentName)
                        ) as T
                    }
                }
            }

            CashbackScreen(
                viewModel = viewModel(factory = vmFactory),
                navigateToCard = {
                    val route = application.appComponent
                        .bankCardFeature()
                        .createDestinationRoute(it)
                    navController.navigate(route) {
                        popUpTo(Cashback.destinationRoute)
                        launchSingleTop = true
                    }
                },
                popBackStack = navController::popBackStack
            )
        }
    }
}