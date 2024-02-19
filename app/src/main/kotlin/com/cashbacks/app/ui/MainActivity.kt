package com.cashbacks.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.cashbacks.app.app.App
import com.cashbacks.app.model.ColorDesignMapper.isDark
import com.cashbacks.app.ui.features.home.HomeFeature
import com.cashbacks.app.ui.theme.CashbacksTheme
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.register
import com.cashbacks.app.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val mainViewModel: MainViewModel = viewModel {
                (application as App).appComponent.mainViewModel()
            }

            CashbacksTheme(settings = mainViewModel.settings.value) {
                val isDarkTheme = mainViewModel.settings.value.colorDesign.isDark

                enableEdgeToEdge(
                    statusBarStyle = mainViewModel.statusBarStyle(isDarkTheme),
                    navigationBarStyle = mainViewModel.navigationBarStyle(isDarkTheme),
                )

                NavHost(modifier = Modifier.fillMaxSize())
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