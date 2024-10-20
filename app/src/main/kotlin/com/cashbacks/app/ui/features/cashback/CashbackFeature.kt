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
import com.cashbacks.app.ui.navigation.FeatureArguments
import com.cashbacks.app.ui.navigation.enterScreenTransition
import com.cashbacks.app.ui.navigation.exitScreenTransition
import com.cashbacks.app.util.getEnum

class CashbackFeature(private val application: App) : FeatureApi {
    private object CashbackArguments : FeatureArguments {
        const val OWNER_TYPE = "parentType"
        const val OWNER_ID = "parentId"
        const val ID = "cashbackId"

        override fun toStringArray() = arrayOf(OWNER_TYPE, OWNER_ID, ID)
    }

    private object Cashback : Feature<CashbackArguments>() {
        override val baseRoute = "cashback"
        override val arguments = CashbackArguments
    }

    fun createDestinationRoute(args: CashbackArgs): String = Cashback.createUrl {
        mapOf(
            ID to args.cashbackId,
            OWNER_ID to args.ownerId,
            OWNER_TYPE to args.ownerType
        )
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
                navArgument(CashbackArguments.OWNER_TYPE) {
                    type = NavType.EnumType(CashbackOwnerType::class.java)
                },
                navArgument(CashbackArguments.OWNER_ID) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(CashbackArguments.ID) {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) {
            val vmFactory = remember {
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return application.appComponent.cashbackViewModel().create(
                            id = it.arguments?.getString(CashbackArguments.ID)?.toLong(),
                            ownerType = it.arguments.getEnum(
                                key = CashbackArguments.OWNER_TYPE,
                                defaultValue = CashbackOwnerType.Category
                            ),
                            ownerId = it.arguments?.getString(CashbackArguments.OWNER_ID)?.toLongOrNull()
                        ) as T
                    }
                }
            }

            CashbackScreen(
                viewModel = viewModel(factory = vmFactory),
                navigateToShop = {
                    val route = application.appComponent
                        .shopFeature()
                        .createDestinationRoute(it)
                    navController.navigate(route) {
                        popUpTo(Cashback.destinationRoute)
                        launchSingleTop = true
                    }
                },
                navigateToCard = {
                    val route = application.appComponent
                        .bankCardFeature()
                        .createDestinationRoute(it)
                    navController.navigate(route) {
                        popUpTo(Cashback.destinationRoute)
                        launchSingleTop = true
                    }
                },
                navigateBack = navController::popBackStack
            )
        }
    }
}