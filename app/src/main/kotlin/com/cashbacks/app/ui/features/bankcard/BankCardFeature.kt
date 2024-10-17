package com.cashbacks.app.ui.features.bankcard

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
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.cashbacks.app.app.App
import com.cashbacks.app.ui.navigation.Feature
import com.cashbacks.app.ui.navigation.FeatureApi
import com.cashbacks.app.ui.navigation.enterScreenTransition
import com.cashbacks.app.ui.navigation.exitScreenTransition

class BankCardFeature(private val application: App) : FeatureApi {
    private sealed class BankCard(type: String) : Feature {
        data object Viewing : BankCard("viewing")
        data object Editing : BankCard("editing")

        override val args = Args
        override val baseRoute: String = buildString {
            append(Root)
            append(type[0].uppercase())
            append(type.substring(1..<type.length))
        }

        fun createUrl(id: Long?): String {
            return "$baseRoute/$id"
        }

        object Args : Feature.Args {
            const val Id = "id"
            override fun toStringArray() = arrayOf(Id)

        }

        companion object {
            const val Root = "bankCard"
        }
    }


    fun createDestinationRoute(args: BankCardArgs): String {
        val route = when {
            args.isEditing || args.id == null -> BankCard.Editing
            else -> BankCard.Viewing
        }
        return route.createUrl(args.id)
    }

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.navigation(
            route = BankCard.Root,
            startDestination = BankCard.Viewing.destinationRoute,
            enterTransition = { enterScreenTransition(expandFrom = Alignment.End) },
            exitTransition = { exitScreenTransition(shrinkTowards = Alignment.Start) },
            popEnterTransition = { enterScreenTransition(expandFrom = Alignment.Start) },
            popExitTransition = { exitScreenTransition(shrinkTowards = Alignment.End) }
        ) {
            composable(
                route = BankCard.Viewing.destinationRoute,
                arguments = listOf(
                    navArgument(BankCard.Args.Id) {
                        type = NavType.LongType
                    }
                )
            ) {
                val vmFactory = remember {
                    object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return application.appComponent.bankCardViewerViewModel().create(
                                cardId = it.arguments?.getLong(BankCard.Args.Id) ?: 0
                            ) as T
                        }
                    }
                }

                BankCardViewingScreen(
                    viewModel = viewModel(factory = vmFactory),
                    navigateToBankCard = {
                        val route = createDestinationRoute(it)
                        navController.navigate(route) {
                            popUpTo(BankCard.Viewing.destinationRoute)
                            launchSingleTop = true
                        }
                    },
                    navigateBack = navController::popBackStack
                )
            }

            composable(
                route = BankCard.Editing.destinationRoute,
                arguments = listOf(
                    navArgument(BankCard.Args.Id) {
                        type = NavType.StringType
                        nullable = true
                    }
                )
            ) {
                val vmFactory = remember {
                    object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return application.appComponent.bankCardEditorViewModel().create(
                                bankCardId = it.arguments
                                    ?.getString(BankCard.Args.Id)
                                    ?.toLongOrNull()
                            ) as T
                        }
                    }
                }

                BankCardEditingScreen(
                    viewModel = viewModel(factory = vmFactory),
                    navigateBack = navController::popBackStack
                )
            }
        }
    }
}