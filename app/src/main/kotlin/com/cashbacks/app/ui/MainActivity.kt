package com.cashbacks.app.ui

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
            CashbacksTheme(settings = mainViewModel.settings.value) { isDarkTheme ->
                val statusBarStyle = when {
                    isDarkTheme.xor(mainViewModel.settings.value.dynamicColor) ->
                        SystemBarStyle.dark(scrim = Color.TRANSPARENT)
                    else -> SystemBarStyle.light(scrim = Color.TRANSPARENT, darkScrim = Color.TRANSPARENT)
                }
                val navigationBarStyle = when {
                    isDarkTheme -> SystemBarStyle.dark(scrim = Color.TRANSPARENT)
                    else -> SystemBarStyle.light(scrim = Color.TRANSPARENT, darkScrim = Color.RED)
                }
                enableEdgeToEdge(
                    statusBarStyle = statusBarStyle,
                    navigationBarStyle = navigationBarStyle,
                )
                NavigationScreen(
                    application = application as App,
                    isDarkTheme = isDarkTheme,
                    viewModel = mainViewModel
                )
            }
        }
    }
}