package com.cashbacks.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cashbacks.app.app.App
import com.cashbacks.app.ui.screens.navigation.NavigationScreen
import com.cashbacks.app.ui.theme.CashbacksTheme
import com.cashbacks.app.viewmodel.BasicViewModelFactory
import com.cashbacks.app.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val mainViewModel: MainViewModel = viewModel(
                factory = BasicViewModelFactory {
                    (application as App).appComponent.mainViewModel()
                }
            )

            CashbacksTheme(settings = mainViewModel.settings.value) { isDarkTheme ->
                enableEdgeToEdge(
                    statusBarStyle = mainViewModel.statusBarStyle(isDarkTheme),
                    navigationBarStyle = mainViewModel.navigationBarStyle(isDarkTheme),
                )
                NavigationScreen(
                    application = application as App,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}