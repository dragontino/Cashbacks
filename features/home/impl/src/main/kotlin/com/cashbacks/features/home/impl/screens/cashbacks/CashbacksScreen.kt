package com.cashbacks.features.home.impl.screens.cashbacks

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DataArray
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cashbacks.common.composables.BasicFloatingActionButton
import com.cashbacks.common.composables.BoundedSnackbar
import com.cashbacks.common.composables.CollapsingToolbarScaffold
import com.cashbacks.common.composables.ConfirmDeletionDialog
import com.cashbacks.common.composables.EmptyList
import com.cashbacks.common.composables.LoadingInBox
import com.cashbacks.common.composables.ModalBottomSheet
import com.cashbacks.common.composables.ModalSheetItems.IconTextItem
import com.cashbacks.common.composables.management.DialogType
import com.cashbacks.common.composables.management.ListState
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.common.composables.management.toListState
import com.cashbacks.common.composables.utils.animate
import com.cashbacks.common.composables.utils.floatingActionButtonEnterAnimation
import com.cashbacks.common.composables.utils.floatingActionButtonExitAnimation
import com.cashbacks.common.composables.utils.keyboardAsState
import com.cashbacks.common.composables.utils.mix
import com.cashbacks.common.composables.utils.reversed
import com.cashbacks.common.resources.R
import com.cashbacks.common.utils.mvi.IntentSender
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.domain.model.CashbackOwner
import com.cashbacks.features.cashback.presentation.api.CashbackArgs
import com.cashbacks.features.cashback.presentation.api.composables.CashbackComposable
import com.cashbacks.features.home.impl.composables.HomeAppBarDefaults
import com.cashbacks.features.home.impl.composables.HomeTopAppBar
import com.cashbacks.features.home.impl.composables.HomeTopAppBarState
import com.cashbacks.features.home.impl.mvi.CashbacksIntent
import com.cashbacks.features.home.impl.mvi.CashbacksLabel
import com.cashbacks.features.home.impl.mvi.CashbacksState
import com.cashbacks.features.home.impl.navigation.HomeDestination
import com.cashbacks.features.home.impl.utils.LocalBottomBarHeight
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun CashbacksRoot(
    openDrawer: () -> Unit,
    navigateToCashback: (args: CashbackArgs) -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CashbacksViewModel = koinViewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember(::SnackbarHostState)
    val dialogType = rememberSaveable { mutableStateOf<DialogType?>(null) }

    LaunchedEffect(Unit) {
        viewModel.labelFlow.collect { label ->
            when (label) {
                is CashbacksLabel.NavigateBack -> navigateBack()
                is CashbacksLabel.NavigateToCashback -> navigateToCashback(label.args)
                is CashbacksLabel.DisplayMessage -> launch {
                    snackbarHostState.showSnackbar(label.message)
                }
                is CashbacksLabel.ChangeOpenedDialog -> dialogType.value = label.type
                is CashbacksLabel.OpenNavigationDrawer -> openDrawer()
            }
        }
    }

    if (dialogType.value is DialogType.ConfirmDeletion<*>) {
        val cashback = (dialogType.value as DialogType.ConfirmDeletion<*>).value as Cashback
        ConfirmDeletionDialog(
            text = stringResource(R.string.confirm_cashback_deletion),
            onConfirm = { viewModel.sendIntent(CashbacksIntent.DeleteCashback(cashback)) },
            onClose = { viewModel.sendIntent(CashbacksIntent.CloseDialog) }
        )
    }

    CashbacksScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        intentSender = IntentSender(viewModel::sendIntent),
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CashbacksScreen(
    state: CashbacksState,
    snackbarHostState: SnackbarHostState,
    intentSender: IntentSender<CashbacksIntent>,
    modifier: Modifier = Modifier
) {
    BackHandler {
        intentSender.sendWithDelay(CashbacksIntent.ClickButtonBack)
    }

    if (state.showBottomSheet) {
        ModalBottomSheet(
            onClose = { intentSender.send(CashbacksIntent.CloseBottomSheet) },
            title = stringResource(R.string.add_cashback_title),
            beautifulDesign = true
        ) {
            IconTextItem(
                icon = HomeDestination.Categories.unselectedIcon,
                iconTintColor = MaterialTheme.colorScheme.primary,
                text = stringResource(R.string.to_category)
            ) {
                intentSender.sendWithDelay(
                    CashbacksIntent.NavigateToCashback(
                        args = CashbackArgs.fromCategory(categoryId = null)
                    )
                )
                intentSender.send(CashbacksIntent.CloseBottomSheet)
            }

            IconTextItem(
                icon = HomeDestination.Shops.unselectedIcon,
                iconTintColor = MaterialTheme.colorScheme.primary,
                text = stringResource(R.string.to_shop)
            ) {
                intentSender.sendWithDelay(
                    CashbacksIntent.NavigateToCashback(
                        args = CashbackArgs.fromShop(shopId = null)
                    )
                )
                intentSender.send(CashbacksIntent.CloseBottomSheet)
            }
        }
    }

    Crossfade(
        targetState = state.screenState,
        label = "cashbacks screen loading animation",
        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
        modifier = modifier
    ) { screenState ->
        when (screenState) {
            ScreenState.Loading -> LoadingInBox(Modifier.padding(bottom = LocalBottomBarHeight.current))

            ScreenState.Stable -> {
                CashbacksScreenContent(
                    state = state,
                    snackbarHostState = snackbarHostState,
                    intentSender = intentSender,
                    modifier = modifier
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CashbacksScreenContent(
    state: CashbacksState,
    snackbarHostState: SnackbarHostState,
    intentSender: IntentSender<CashbacksIntent>,
    modifier: Modifier = Modifier
) {
    val topBarState = rememberTopAppBarState()
    val lazyListState = rememberLazyListState()
    val keyboardState = keyboardAsState()
    val fabHeightPx = rememberSaveable { mutableFloatStateOf(0f) }

    CollapsingToolbarScaffold(
        topBar = {
            HomeTopAppBar(
                title = HomeDestination.Cashbacks.screenTitle,
                state = state.appBarState,
                onStateChange = { intentSender.send(CashbacksIntent.ChangeAppBarState(it)) },
                searchPlaceholder = stringResource(R.string.search_cashbacks_placeholder),
                onNavigationIconClick = {
                    intentSender.sendWithDelay(CashbacksIntent.ClickNavigationButton)
                },
                colors = HomeAppBarDefaults.colors(
                    topBarContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = .6f)
                        .mix(MaterialTheme.colorScheme.primary)
                        .ratio(topBarState.overlappedFraction)
                )
            )
        },
        topBarState = topBarState,
        contentState = lazyListState,
        topBarScrollEnabled = state.appBarState is HomeTopAppBarState.TopBar,
        snackbarHost = {
            SnackbarHost(snackbarHostState) {
                BoundedSnackbar(
                    snackbarData = it,
                    containerColor = MaterialTheme.colorScheme.background.reversed.animate(),
                    contentColor = MaterialTheme.colorScheme.onBackground.reversed.animate(),
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        floatingActionButtons = {
            AnimatedVisibility(
                visible = !keyboardState.value,
                enter = floatingActionButtonEnterAnimation(),
                exit = floatingActionButtonExitAnimation()
            ) {
                BasicFloatingActionButton(icon = Icons.Rounded.Add) {
                    intentSender.sendWithDelay(CashbacksIntent.OpenBottomSheet)
                }
            }
        },
        fabModifier = Modifier
            .windowInsetsPadding(WindowInsets.tappableElement.only(WindowInsetsSides.End))
            .padding(bottom = LocalBottomBarHeight.current)
            .onGloballyPositioned { fabHeightPx.floatValue = it.size.height.toFloat() },
        contentWindowInsets = WindowInsets.ime.only(WindowInsetsSides.Bottom),
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
    ) {
        CashbacksList(
            state = state,
            contentState = lazyListState,
            contentPadding = PaddingValues(
                bottom = with(LocalDensity.current) {
                    (LocalBottomBarHeight.current + fabHeightPx.floatValue.toDp()).animate()
                }
            ),
            intentSender = intentSender
        )
    }
}


@Composable
private fun CashbacksList(
    state: CashbacksState,
    contentState: LazyListState,
    contentPadding: PaddingValues,
    intentSender: IntentSender<CashbacksIntent>,
    modifier: Modifier = Modifier
) {
    Crossfade(
        targetState = state.cashbacks.toListState(),
        label = "loading anim",
        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
        modifier = modifier.fillMaxSize()
    ) { listState ->
        when (listState) {
            is ListState.Loading -> LoadingInBox(Modifier.padding(contentPadding))

            is ListState.Empty -> EmptyList(
                text = when (state.appBarState) {
                    is HomeTopAppBarState.Search -> {
                        if (state.appBarState.query.isBlank()) {
                            stringResource(R.string.empty_search_query)
                        } else {
                            stringResource(
                                R.string.empty_search_results,
                                state.appBarState.query
                            )
                        }
                    }

                    else -> stringResource(R.string.no_cashbacks)
                },
                icon = Icons.Rounded.DataArray,
                iconModifier = Modifier.scale(2.5f),
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
            )

            is ListState.Stable -> LazyColumn(
                state = contentState,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                contentPadding.calculateTopPadding().takeIf { it.value > 0 }?.let {
                    item {
                        Spacer(Modifier.height(it))
                    }
                }

                items(listState.data) { cashback ->
                    CashbackComposable(
                        cashback = cashback,
                        isEnabledToSwipe = state.swipedCashbackId == cashback.id || state.swipedCashbackId == null,
                        onSwipeStatusChanged = { isOnSwipe ->
                            intentSender.sendWithDelay(
                                CashbacksIntent.SwipeCashback(cashback.id, isOnSwipe)
                            )
                        },
                        onClick = {
                            val args = when (cashback.owner) {
                                is CashbackOwner.Category -> CashbackArgs.fromCategory(
                                    categoryId = cashback.owner.id,
                                    cashbackId = cashback.id
                                )

                                is CashbackOwner.Shop -> CashbackArgs.fromShop(
                                    cashbackId = cashback.id,
                                    shopId = cashback.owner.id
                                )
                            }
                            intentSender.sendWithDelay(CashbacksIntent.NavigateToCashback(args))
                        },
                        onDelete = {
                            intentSender.sendWithDelay(
                                CashbacksIntent.OpenDialog(DialogType.ConfirmDeletion(cashback))
                            )
                        },
                        modifier = Modifier.padding(
                            start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                            end = contentPadding.calculateEndPadding(LocalLayoutDirection.current)
                        )
                    )
                }

                contentPadding.calculateBottomPadding().takeIf { it.value > 0 }?.let {
                    item {
                        Spacer(Modifier.height(it))
                    }
                }
            }
        }
    }
}