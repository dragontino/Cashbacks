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
import com.cashbacks.app.util.getEnum

class CashbackFeature(private val application: App) : FeatureApi {
    private object Cashback : Feature {
        object Args : Feature.Args {
            const val OWNER_TYPE = "parentType"
            const val OWNER_ID = "parentId"
            const val CASHBACK_ID = "cashbackId"

            override fun toStringArray() = arrayOf(OWNER_TYPE, OWNER_ID, CASHBACK_ID)
        }


        override val baseRoute = "cashback"
        override val args = Args
    }

    fun createDestinationRoute(args: CashbackArgs): String {
        return "${Cashback.baseRoute}/${args.ownerType}/${args.ownerId}/${args.cashbackId}"
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
                navArgument(Cashback.Args.OWNER_TYPE) {
                    type = NavType.EnumType(CashbackOwner::class.java)
                },
                navArgument(Cashback.Args.OWNER_ID) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(Cashback.Args.CASHBACK_ID) {
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
                            id = it.arguments?.getString(Cashback.Args.CASHBACK_ID)?.toLong(),
                            ownerType = it.arguments.getEnum(Cashback.Args.OWNER_TYPE, CashbackOwner.Category),
                            ownerId = it.arguments?.getString(Cashback.Args.OWNER_ID)?.toLongOrNull()
                        ) as T
                    }
                }
            }

            CashbackScreen(
                viewModel = viewModel(factory = vmFactory),
                navigateToCategory = {
                    val route = application.appComponent
                        .categoryFeature()
                        .createDestinationRoute(it)
                    navController.navigate(route) {
                        popUpTo(Cashback.destinationRoute)
                        launchSingleTop = true
                    }
                },
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
                popBackStack = navController::popBackStack
            )
        }
    }
}