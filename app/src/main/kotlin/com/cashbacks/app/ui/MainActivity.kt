package com.cashbacks.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.cashbacks.app.app.App
import com.cashbacks.app.ui.features.home.HomeFeature
import com.cashbacks.app.ui.theme.CashbacksTheme
import com.cashbacks.app.util.ColorDesignUtils.isDark
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.register
import com.cashbacks.app.util.reversed
import com.cashbacks.app.viewmodel.MainViewModel
import com.cashbacks.domain.R
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val mainViewModel: MainViewModel = viewModel {
                (application as App).appComponent.mainViewModel()
            }

            val snackbarHostState = remember(::SnackbarHostState)
            val scope = rememberCoroutineScope()

            val showSnackbar = remember {
                fun(message: String) {
                    scope.launch { snackbarHostState.showSnackbar(message) }
                }
            }


            LaunchedEffect(Unit) {
                if (
                    (application as App).checkExpiredCashbacks &&
                    mainViewModel.settings.value.autoDeleteExpiredCashbacks
                ) {
                    mainViewModel.deleteExpiredCashbacks(
                        success = {
                            showSnackbar(application.getString(R.string.expired_cashbacks_deletion_success))
                        },
                        failure = showSnackbar
                    )
                    (application as App).checkExpiredCashbacks = false
                }
            }

            CashbacksTheme(settings = mainViewModel.settings.value) {
                val isDarkTheme = mainViewModel.settings.value.colorDesign.isDark

                enableEdgeToEdge(
                    statusBarStyle = mainViewModel.statusBarStyle(isDarkTheme),
                    navigationBarStyle = mainViewModel.navigationBarStyle(isDarkTheme),
                )

                Box {
                    NavHost(modifier = Modifier
                        .zIndex(1f)
                        .fillMaxSize()
                    )

                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier
                            .padding(bottom = 180.dp)
                            .align(Alignment.BottomCenter)
                            .zIndex(2f)
                    ) {
                        Snackbar(
                            snackbarData = it,
                            shape = MaterialTheme.shapes.medium,
                            containerColor = MaterialTheme.colorScheme.background.reversed.animate(),
                            contentColor = MaterialTheme.colorScheme.onBackground.reversed.animate()
                        )
                    }
                }
            }
        }
    }


    @Composable
    private fun NavHost(modifier: Modifier = Modifier) {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = HomeFeature.Home.destinationRoute,
            modifier = Modifier.background(MaterialTheme.colorScheme.background.animate())
        ) {
            register(
                feature = (application as App).appComponent.homeFeature(),
                navController = navController,
                modifier = modifier
            )

            register(
                feature = (application as App).appComponent.settingsFeature(),
                navController = navController,
                modifier = modifier
            )

            register(
                feature = (application as App).appComponent.categoryFeature(),
                navController = navController,
                modifier = modifier
            )

            register(
                feature = (application as App).appComponent.shopFeature(),
                navController = navController,
                modifier = modifier
            )

            register(
                feature = (application as App).appComponent.cashbackFeature(),
                navController = navController,
                modifier = modifier
            )

            register(
                feature = (application as App).appComponent.bankCardFeature(),
                navController = navController,
                modifier = modifier
            )
        }
    }
}