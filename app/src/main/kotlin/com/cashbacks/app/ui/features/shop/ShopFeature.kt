package com.cashbacks.app.ui.features.shop

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

class ShopFeature(private val application: App) : FeatureApi {
    object Shop : Feature {
        object Args : Feature.Args {
            const val ShopId = "shopId"
            const val IsEditing = "isEditing"
            override fun toStringArray() = arrayOf(ShopId, IsEditing)

        }

        override val baseRoute = "shop"
        override val args = Args
    }

    fun createDestinationRoute(args: ShopArgs) =
        "${Shop.baseRoute}/${args.shopId}/${args.isEditing}"

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.composable(
            route = Shop.destinationRoute,
            enterTransition = { enterScreenTransition(expandFrom = Alignment.End) },
            exitTransition = { exitScreenTransition(shrinkTowards = Alignment.Start) },
            popEnterTransition = { enterScreenTransition(expandFrom = Alignment.Start) },
            popExitTransition = { exitScreenTransition(shrinkTowards = Alignment.End) },
            arguments = listOf(
                navArgument(Shop.Args.ShopId) {
                    type = NavType.LongType
                },
                navArgument(Shop.Args.IsEditing) {
                    type = NavType.BoolType
                }
            )
        ) {
            val vmFactory = remember {
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return application.appComponent.shopViewModel().create(
                            application = application,
                            shopId = it.arguments?.getLong(Shop.Args.ShopId) ?: 0,
                            isEditing = it.arguments?.getBoolean(Shop.Args.IsEditing) ?: false
                        ) as T
                    }
                }
            }

            ShopScreen(
                viewModel = viewModel(factory = vmFactory),
                popBackStack = navController::popBackStack,
                navigateToCashback = {
                    val route = application.appComponent
                        .cashbackFeature()
                        .createDestinationRoute(it)
                    navController.navigate(route) {
                        popUpTo(Shop.destinationRoute)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}