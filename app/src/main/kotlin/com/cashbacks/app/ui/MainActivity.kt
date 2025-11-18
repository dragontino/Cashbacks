package com.cashbacks.app.ui

import android.Manifest
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.cashbacks.app.viewmodel.MainViewModel
import com.cashbacks.common.composables.AppLaunchPermissionsDialog
import com.cashbacks.common.composables.BoundedSnackbar
import com.cashbacks.common.composables.Loading
import com.cashbacks.common.composables.theme.CashbacksTheme
import com.cashbacks.common.composables.utils.animate
import com.cashbacks.common.composables.utils.loadingContentAnimationSpec
import com.cashbacks.common.composables.utils.reversed
import com.cashbacks.common.navigation.utils.register
import com.cashbacks.common.resources.R
import com.cashbacks.core.database.utils.DatabaseMigrator
import com.cashbacks.features.bankcard.presentation.impl.navigation.BankCardFeature
import com.cashbacks.features.cashback.presentation.impl.navigation.CashbackFeature
import com.cashbacks.features.category.presentation.impl.navigation.CategoryFeature
import com.cashbacks.features.home.api.Home
import com.cashbacks.features.home.impl.navigation.HomeFeature
import com.cashbacks.features.settings.domain.model.Settings
import com.cashbacks.features.settings.presentation.navigation.SettingsFeature
import com.cashbacks.features.settings.presentation.utils.isDark
import com.cashbacks.features.shop.presentation.impl.navigation.ShopFeature
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    private val databaseMigrator: DatabaseMigrator by inject()
    private val snackbarHostState by lazy { SnackbarHostState() }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val mainViewModel: MainViewModel = koinViewModel()
            val settings by mainViewModel.settingsStateFlow.collectAsStateWithLifecycle()

            val isDarkTheme = settings.colorDesign.isDark
            CashbacksTheme(isDarkTheme, settings.dynamicColor) {
                enableEdgeToEdge(
                    statusBarStyle = statusBarStyle(settings),
                    navigationBarStyle = navigationBarStyle(settings),
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    AppLaunchPermissionsDialog(Manifest.permission.POST_NOTIFICATIONS) {
                        showSnackbar(getString(R.string.permission_required))
                    }
                }

                Box {
                    MainScreen(
                        modifier = Modifier
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
                        BoundedSnackbar(
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


    @Stable
    @Composable
    private fun statusBarStyle(settings: Settings): SystemBarStyle {
        val isDarkTheme = settings.colorDesign.isDark
        val dynamicColor = settings.dynamicColor
        return when {
            isDarkTheme.xor(dynamicColor) -> SystemBarStyle.dark(scrim = Color.TRANSPARENT)
            else -> SystemBarStyle.light(scrim = Color.TRANSPARENT, darkScrim = Color.TRANSPARENT)
        }
    }


    @Stable
    @Composable
    private fun navigationBarStyle(settings: Settings): SystemBarStyle {
        val isDarkTheme = settings.colorDesign.isDark
        return when {
            isDarkTheme -> SystemBarStyle.dark(scrim = Color.TRANSPARENT)
            else -> SystemBarStyle.light(scrim = Color.TRANSPARENT, darkScrim = Color.TRANSPARENT)
        }
    }
    
    
    @Composable
    private fun MainScreen(modifier: Modifier = Modifier) {
        Crossfade(
            targetState = databaseMigrator.needToMigrate(),
            animationSpec = loadingContentAnimationSpec(),
            modifier = modifier.fillMaxSize()
        ) { needToMigrate ->

            if (needToMigrate) {
                LaunchedEffect(Unit) {
                    do {
                        val result = databaseMigrator.migrate().onFailure {
                            it.localizedMessage?.let(::showSnackbar)
                        }
                    } while (result.isFailure)
                }

                DatabaseMigrateLoading()
            } else {
                NavHost()
            }
        }
    }


    private val features = arrayOf(
        HomeFeature,
        SettingsFeature,
        CategoryFeature,
        ShopFeature,
        CashbackFeature,
        BankCardFeature,
    )


    @Composable
    private fun NavHost(modifier: Modifier = Modifier) {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = Home,
            modifier = Modifier.background(MaterialTheme.colorScheme.background.animate())
        ) {
            features.forEach { feature ->
                register(
                    feature = feature,
                    navController = navController,
                    modifier = modifier
                )
            }
        }
    }


    @Composable
    private fun DatabaseMigrateLoading(modifier: Modifier = Modifier) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .background(MaterialTheme.colorScheme.background.animate())
                .fillMaxSize()
        ) {
            Loading(Modifier.scale(1.4f))
            Spacer(Modifier.padding(16.dp))
            Text("Шифруем базу данных…")
        }
    }


    private fun showSnackbar(message: String) {
        lifecycleScope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }
}