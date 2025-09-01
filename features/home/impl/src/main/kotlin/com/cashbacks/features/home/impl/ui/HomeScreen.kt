package com.cashbacks.features.home.impl.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cashbacks.common.composables.BoundedSnackbar
import com.cashbacks.common.composables.ModalSheetDefaults
import com.cashbacks.common.composables.ModalSheetItems.IconTextItem
import com.cashbacks.common.composables.theme.VerdanaFont
import com.cashbacks.common.composables.utils.Saver
import com.cashbacks.common.composables.utils.animate
import com.cashbacks.common.composables.utils.getActivity
import com.cashbacks.common.composables.utils.mutableStateSaver
import com.cashbacks.common.navigation.enterScreenTransition
import com.cashbacks.common.navigation.exitScreenTransition
import com.cashbacks.common.resources.R
import com.cashbacks.features.bankcard.presentation.api.BankCardArgs
import com.cashbacks.features.cashback.presentation.api.CashbackArgs
import com.cashbacks.features.category.presentation.api.CategoryArgs
import com.cashbacks.features.home.impl.composables.ModalNavigationDrawerContent
import com.cashbacks.features.home.impl.mvi.HomeIntent
import com.cashbacks.features.home.impl.mvi.HomeLabel
import com.cashbacks.features.home.impl.mvi.HomeState
import com.cashbacks.features.home.impl.navigation.HomeDestination
import com.cashbacks.features.home.impl.screens.cards.BankCardsRoot
import com.cashbacks.features.home.impl.screens.cashbacks.CashbacksRoot
import com.cashbacks.features.home.impl.screens.categories.CategoriesRoot
import com.cashbacks.features.home.impl.screens.shops.ShopsRoot
import com.cashbacks.features.home.impl.utils.LocalBottomBarHeight
import com.cashbacks.features.home.impl.utils.SnackbarAction
import com.cashbacks.features.home.impl.viewmodel.HomeViewModel
import com.cashbacks.features.shop.presentation.api.ShopArgs
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Suppress("DEPRECATION")
@Composable
internal fun HomeRoot(
    navigateToSettings: () -> Unit,
    navigateToCategory: (args: CategoryArgs) -> Unit,
    navigateToShop: (args: ShopArgs) -> Unit,
    navigateToCashback: (args: CashbackArgs) -> Unit,
    navigateToCard: (args: BankCardArgs) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember(::SnackbarHostState)
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.labelFlow.collect { label ->
            when (label) {
                is HomeLabel.DisplayMessage -> launch {
                    val result = snackbarHostState.showSnackbar(
                        message = label.message,
                        actionLabel = label.action?.label
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        label.action?.onClick?.invoke()
                    }
                }
                is HomeLabel.NavigateToSettings -> navigateToSettings()
                is HomeLabel.NavigateToCategory -> navigateToCategory(label.args)
                is HomeLabel.NavigateToShop -> navigateToShop(label.args)
                is HomeLabel.NavigateToCashback -> navigateToCashback(label.args)
                is HomeLabel.NavigateToBankCard -> navigateToCard(label.args)

                HomeLabel.OpenDrawer -> drawerState.animateTo(
                    targetValue = DrawerValue.Open,
                    anim = ModalSheetDefaults.drawerAnimationSpec
                )

                HomeLabel.CloseDrawer -> drawerState.animateTo(
                    targetValue = DrawerValue.Closed,
                    anim = ModalSheetDefaults.drawerAnimationSpec
                )

                is HomeLabel.OpenExternalFolder -> openExternalFolder(context, label.path)
            }
        }
    }

    HomeScreen(
        state = state,
        drawerState = drawerState,
        snackbarHostState = snackbarHostState,
        sendIntent = viewModel::sendIntent,
        modifier = modifier
    )
}


private fun openExternalFolder(context: Context, path: String) {
    val uri = path.toUri()
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(uri, "text/csv")
    context.startActivity(
        Intent.createChooser(intent, context.getString(R.string.open) + "…")
    )
}


@Composable
private fun HomeScreen(
    state: HomeState,
    drawerState: DrawerState,
    snackbarHostState: SnackbarHostState,
    sendIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            sendIntent(
                HomeIntent.ClickButtonExportData { path ->
                    val message = context.getString(R.string.data_exported, path)
                    val action = SnackbarAction(context.getString(R.string.open)) {
                        sendIntent(HomeIntent.OpenExternalFolder(path))
                    }
                    sendIntent(HomeIntent.ShowMessage(message, action))
                }
            )
        } else {
            sendIntent(HomeIntent.ShowMessage(context.getString(R.string.permission_required)))
        }
    }

    val density = LocalDensity.current
    val navController = rememberNavController()
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = remember {
        derivedStateOf {
            listOf(
                HomeDestination.Categories,
                HomeDestination.Shops,
                HomeDestination.Cashbacks,
                HomeDestination.Cards
            ).find {
                currentBackStackEntry.value?.destination?.hasRoute(it::class) == true
            } ?: HomeDestination.Categories
        }
    }


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalNavigationDrawerContent(appInfo = state.appInfo) {
                IconTextItem(
                    text = stringResource(R.string.home),
                    icon = Icons.Rounded.Home,
                    iconTintColor = MaterialTheme.colorScheme.primary.animate(),
                    selected = true,
                    onClick = { sendIntent(HomeIntent.ClickButtonCloseDrawer) }
                )

                HorizontalDivider(Modifier.padding(top = 8.dp, bottom = 4.dp))

                IconTextItem(
                    text = stringResource(R.string.settings),
                    icon = Icons.Rounded.Settings,
                    iconTintColor = MaterialTheme.colorScheme.primary.animate(),
                    selected = false
                ) {
                    sendIntent(HomeIntent.ClickButtonCloseDrawer)
                    sendIntent(HomeIntent.ClickButtonOpenSettings)
                }

                IconTextItem(
                    text = stringResource(R.string.export_data),
                    icon = Icons.Rounded.Download,
                    iconTintColor = MaterialTheme.colorScheme.primary.animate()
                ) {
                    sendIntent(HomeIntent.ClickButtonCloseDrawer)
                    usePermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        context = context,
                        onGranted = {
                            sendIntent(
                                HomeIntent.ClickButtonExportData { path ->
                                    val message = context.getString(R.string.data_exported, path)
                                    val action = SnackbarAction(context.getString(R.string.open)) {
                                        sendIntent(HomeIntent.OpenExternalFolder(path))
                                    }
                                    sendIntent(HomeIntent.ShowMessage(message, action))
                                }
                            )
                        },
                        onDenied = { permissionLauncher.launch(it) }
                    )
                }
            }
        },
        gesturesEnabled = drawerState.isOpen,
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
    ) {
        val bottomBarHeight = rememberSaveable(saver = mutableStateSaver(Dp.Saver)) {
            mutableStateOf(0.dp)
        }

        CompositionLocalProvider(LocalBottomBarHeight provides bottomBarHeight.value) {
            Box(
                modifier = Modifier
                    .then(modifier)
                    .fillMaxSize()
            ) {
                NavHost(
                    navController = navController,
                    startDestination = HomeDestination.Categories,
                    modifier = Modifier
                        .zIndex(1.1f)
                        .fillMaxSize(),
                ) {
                    composable<HomeDestination.Categories>(
                        enterTransition = { enterScreenTransition(expandFrom = Alignment.Start) },
                        exitTransition = { exitScreenTransition(shrinkTowards = Alignment.Start) }
                    ) {
                        CategoriesRoot(
                            openDrawer = { sendIntent(HomeIntent.ClickButtonOpenDrawer) },
                            navigateToCategory = { sendIntent(HomeIntent.NavigateToCategory(it)) },
                            navigateToCashback = { sendIntent(HomeIntent.NavigateToCashback(it)) },
                            navigateBack = { context.getActivity()?.finish() }
                        )
                    }

                    composable<HomeDestination.Shops>(
                        enterTransition = {
                            val expandFrom = when {
                                initialState.destination.hasRoute<HomeDestination.Categories>() -> Alignment.End
                                else -> Alignment.Start
                            }
                            enterScreenTransition(expandFrom)
                        },
                        exitTransition = {
                            val shrinkTowards = when {
                                targetState.destination.hasRoute<HomeDestination.Categories>() -> Alignment.End
                                else -> Alignment.Start
                            }
                            exitScreenTransition(shrinkTowards)
                        },
                    ) {
                        ShopsRoot(
                            openDrawer = { sendIntent(HomeIntent.ClickButtonOpenDrawer) },
                            navigateToShop = { sendIntent(HomeIntent.NavigateToShop(it)) },
                            navigateToCashback = { sendIntent(HomeIntent.NavigateToCashback(it)) },
                            navigateBack = navController::popBackStack,
                            )
                    }

                    composable<HomeDestination.Cashbacks>(
                        enterTransition = {
                            val expandFrom = when {
                                initialState.destination.hasRoute<HomeDestination.Cards>() -> Alignment.Start
                                else -> Alignment.End
                            }
                            enterScreenTransition(expandFrom)
                        },
                        exitTransition = {
                            val shrinkTowards = when {
                                targetState.destination.hasRoute<HomeDestination.Cards>() -> Alignment.Start
                                else -> Alignment.End
                            }
                            exitScreenTransition(shrinkTowards)
                        }
                    ) {
                        CashbacksRoot(
                            openDrawer = { sendIntent(HomeIntent.ClickButtonOpenDrawer) },
                            navigateToCashback = { sendIntent(HomeIntent.NavigateToCashback(it)) },
                            navigateBack = navController::popBackStack,
                        )
                    }

                    composable<HomeDestination.Cards>(
                        enterTransition = { enterScreenTransition(expandFrom = Alignment.End) },
                        exitTransition = { exitScreenTransition(shrinkTowards = Alignment.End) }
                    ) {
                        BankCardsRoot(
                            openDrawer = { sendIntent(HomeIntent.ClickButtonOpenDrawer) },
                            navigateToCard = { sendIntent(HomeIntent.NavigateToBankCard(it)) },
                        )
                    }
                }


                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .zIndex(1.4f)
                        .padding(horizontal = 8.dp)
                        .padding(
                            bottom = with(LocalDensity.current) { bottomBarHeight.value }
                        )
                        .align(Alignment.BottomCenter)
                ) {
                    BoundedSnackbar(it)
                }


                BottomBar(
                    selectedDestination = currentDestination.value,
                    onClickToDestination = {
                        if (navController.currentDestination?.hasRoute(it::class) != true) {
                            navController.navigate(it) {
                                popUpTo<HomeDestination.Categories>()
                                launchSingleTop = true
                            }
                        }
                    },
                    modifier = Modifier
                        .onSizeChanged {
                            with(density) {
                                bottomBarHeight.value = it.height.toDp()
                            }
                        }
                        .zIndex(1.2f)
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                )
            }
        }
    }
}



@Suppress("SameParameterValue")
private inline fun usePermissions(
    vararg permissions: String,
    context: Context,
    onGranted: () -> Unit,
    onDenied: (permission: String) -> Unit
) {
    val isAllPermissionsGranted = permissions.all { permission ->
        val checkPermission = ContextCompat.checkSelfPermission(context, permission)
        if (checkPermission == PackageManager.PERMISSION_DENIED) {
            onDenied(permission)
        }
        return@all checkPermission == PackageManager.PERMISSION_GRANTED
    }

    if (isAllPermissionsGranted) {
        onGranted()
    }
}



@Composable
private fun BottomBar(
    selectedDestination: HomeDestination,
    onClickToDestination: (HomeDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.background
            .copy(alpha = .6f)
            .animate(),
        contentColor = MaterialTheme.colorScheme.onBackground.animate(),
        modifier = modifier.fillMaxWidth()
    ) {
        arrayOf(
            HomeDestination.Categories,
            HomeDestination.Shops,
            HomeDestination.Cashbacks,
            HomeDestination.Cards
        ).forEach { destination ->
            NavigationBarItem(
                selected = destination == selectedDestination,
                onClick = { onClickToDestination(destination) },
                icon = {
                    Crossfade(
                        targetState = destination,
                        label = "bottom bar icon anim",
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                    ) {
                        Icon(
                            imageVector = when(it) {
                                selectedDestination -> it.selectedIcon
                                else -> it.unselectedIcon
                            },
                            contentDescription = null,
                            modifier = Modifier.height(35.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = destination.tabTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily(VerdanaFont),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary.animate(),
                    selectedTextColor = MaterialTheme.colorScheme.primary.animate(),
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = MaterialTheme.colorScheme.onBackground.animate(),
                    unselectedTextColor = MaterialTheme.colorScheme.onBackground.animate()
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}