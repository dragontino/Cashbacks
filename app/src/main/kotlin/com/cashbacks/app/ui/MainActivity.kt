package com.cashbacks.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import com.cashbacks.app.app.App
import com.cashbacks.app.ui.screens.navigation.NavigationScreen
import com.cashbacks.app.ui.theme.CashbacksTheme
import com.cashbacks.app.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vmFactory = (application as App).viewModelFactory
        val mainViewModel by viewModels<MainViewModel> { vmFactory }

        setContent {
            CashbacksTheme(settings = mainViewModel.settings) { isDarkTheme ->
                val statusBarStyle = SystemBarStyle.auto(
                    lightScrim = MaterialTheme.colorScheme.onPrimary.toArgb(),
                    darkScrim = MaterialTheme.colorScheme.onPrimary.toArgb(),
                    detectDarkMode = { isDarkTheme.xor(mainViewModel.settings.dynamicColor) }
                )
                enableEdgeToEdge(statusBarStyle = statusBarStyle)
                NavigationScreen(
                    application = application as App,
                    isDarkTheme = isDarkTheme,
                    viewModel = mainViewModel
                )
            }
        }
    }
}