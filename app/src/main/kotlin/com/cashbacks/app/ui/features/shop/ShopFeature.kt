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
            const val SHOP_ID = "shopId"
            const val IS_EDITING = "isEditing"
            override fun toStringArray() = arrayOf(SHOP_ID, IS_EDITING)

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
                navArgument(Shop.Args.SHOP_ID) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(Shop.Args.IS_EDITING) {
                    type = NavType.BoolType
                }
            )
        ) {
            val vmFactory = remember {
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return application.appComponent.shopViewModel().create(
                            shopId = it.arguments?.getString(ShopArguments.ID)?.toLongOrNull(),
                            isEditing = it.arguments?.getBoolean(ShopArguments.EDITING) ?: false
                        ) as T
                    }
                }
            }

            ShopScreen(
                viewModel = viewModel(factory = vmFactory),
                navigateBack = navController::popBackStack,
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