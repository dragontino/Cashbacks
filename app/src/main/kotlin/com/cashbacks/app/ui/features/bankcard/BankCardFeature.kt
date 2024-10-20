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
import com.cashbacks.app.ui.navigation.FeatureArguments
import com.cashbacks.app.ui.navigation.enterScreenTransition
import com.cashbacks.app.ui.navigation.exitScreenTransition

class BankCardFeature(private val application: App) : FeatureApi {
    private object CardArguments : FeatureArguments {
        const val ID = "id"
        override fun toStringArray() = arrayOf(ID)
    }

    private sealed class BankCard(type: String) : Feature<CardArguments>() {
        data object Viewing : BankCard("viewing")
        data object Editing : BankCard("editing")

        override val arguments = CardArguments
        override val baseRoute: String = buildString {
            append(ROOT)
            append(type[0].uppercase())
            append(type.substring(1..<type.length))
        }

        companion object {
            const val ROOT = "bankCard"
        }
    }


    fun createDestinationRoute(args: BankCardArgs): String {
        val route = when {
            args.isEditing || args.cardId == null -> BankCard.Editing
            else -> BankCard.Viewing
        }
        return route.createUrl { mapOf(ID to args.cardId) }
    }

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.navigation(
            route = BankCard.ROOT,
            startDestination = BankCard.Viewing.destinationRoute,
            enterTransition = { enterScreenTransition(expandFrom = Alignment.End) },
            exitTransition = { exitScreenTransition(shrinkTowards = Alignment.Start) },
            popEnterTransition = { enterScreenTransition(expandFrom = Alignment.Start) },
            popExitTransition = { exitScreenTransition(shrinkTowards = Alignment.End) }
        ) {
            composable(
                route = BankCard.Viewing.destinationRoute,
                arguments = listOf(
                    navArgument(CardArguments.ID) {
                        type = NavType.LongType
                    }
                )
            ) {
                val vmFactory = remember {
                    object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return application.appComponent.bankCardViewerViewModel().create(
                                cardId = it.arguments?.getLong(CardArguments.ID) ?: 0
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
                    navArgument(CardArguments.ID) {
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
                                    ?.getString(CardArguments.ID)
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