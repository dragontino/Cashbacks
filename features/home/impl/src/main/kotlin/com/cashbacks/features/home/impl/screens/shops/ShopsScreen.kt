package com.cashbacks.features.home.impl.screens.shops

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DataArray
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditOff
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
import com.cashbacks.common.composables.utils.animate
import com.cashbacks.common.composables.utils.floatingActionButtonEnterAnimation
import com.cashbacks.common.composables.utils.floatingActionButtonExitAnimation
import com.cashbacks.common.composables.utils.keyboardAsState
import com.cashbacks.common.composables.utils.mix
import com.cashbacks.common.composables.utils.reversed
import com.cashbacks.common.resources.R
import com.cashbacks.common.composables.management.DialogType
import com.cashbacks.common.composables.management.ListState
import com.cashbacks.common.composables.management.ViewModelState
import com.cashbacks.common.composables.management.toListState
import com.cashbacks.features.cashback.domain.utils.asCashbackOwner
import com.cashbacks.features.cashback.presentation.api.CashbackArgs
import com.cashbacks.features.cashback.presentation.api.composables.MaxCashbackOwnerComposable
import com.cashbacks.features.home.impl.composables.HomeAppBarDefaults
import com.cashbacks.features.home.impl.composables.HomeTopAppBar
import com.cashbacks.features.home.impl.composables.HomeTopAppBarState
import com.cashbacks.features.home.impl.mvi.ShopsIntent
import com.cashbacks.features.home.impl.mvi.ShopsLabel
import com.cashbacks.features.home.impl.mvi.ShopsState
import com.cashbacks.features.home.impl.navigation.HomeDestination
import com.cashbacks.features.home.impl.utils.copy
import com.cashbacks.features.shop.domain.model.Shop
import com.cashbacks.features.shop.presentation.api.ShopArgs
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun ShopsRoot(
    openDrawer: () -> Unit,
    navigateToShop: (args: ShopArgs) -> Unit,
    navigateToCashback: (args: CashbackArgs) -> Unit,
    navigateBack: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: ShopsViewModel = koinViewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember(::SnackbarHostState)
    val dialogType = rememberSaveable { mutableStateOf<DialogType?>(null) }

    LaunchedEffect(Unit) {
        viewModel.labelFlow.collect { label ->
            when (label) {
                is ShopsLabel.OpenDialog -> dialogType.value = label.type
                is ShopsLabel.CloseDialog -> dialogType.value = null
                is ShopsLabel.NavigateBack -> navigateBack()
                is ShopsLabel.NavigateToShop -> navigateToShop(label.args)
                is ShopsLabel.OpenNavigationDrawer -> openDrawer()
                is ShopsLabel.NavigateToCashback -> navigateToCashback(label.args)
                is ShopsLabel.DisplayMessage -> launch {
                    snackbarHostState.showSnackbar(label.message)
                }
            }
        }
    }
    
    if (dialogType.value is DialogType.ConfirmDeletion<*>) {
        val shop = (dialogType.value as DialogType.ConfirmDeletion<*>).value as Shop
        ConfirmDeletionDialog(
            text = stringResource(R.string.confirm_shop_deletion, shop.name),
            onConfirm = { viewModel.sendIntent(ShopsIntent.DeleteShop(shop)) },
            onClose = { viewModel.sendIntent(ShopsIntent.CloseDialog) }
        )
    }
    
    ShopsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        contentPadding = contentPadding,
        sendIntent = viewModel::sendIntent,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ShopsScreen(
    state: ShopsState,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues,
    sendIntent: (ShopsIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        when (state.viewModelState) {
            ViewModelState.Editing -> sendIntent(ShopsIntent.FinishEdit)
            ViewModelState.Viewing -> sendIntent(ShopsIntent.ClickButtonBack)
        }
    }

    val topAppBarState = rememberTopAppBarState()
    val lazyListState = rememberLazyListState()
    val keyboardState = keyboardAsState()
    

    val fabHeightPx = rememberSaveable { mutableFloatStateOf(0f) }

    CollapsingToolbarScaffold(
        topBar = {
            HomeTopAppBar(
                title = HomeDestination.Shops.screenTitle,
                state = state.appBarState,
                onStateChange = {
                    sendIntent(ShopsIntent.ChangeAppBarState(it))
                },
                searchPlaceholder = stringResource(R.string.search_shops_placeholder),
                onNavigationIconClick = { sendIntent(ShopsIntent.ClickNavigationButton) },
                colors = HomeAppBarDefaults.colors(
                    topBarContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = .6f)
                        .mix(MaterialTheme.colorScheme.primary)
                        .ratio(topAppBarState.overlappedFraction)
                )
            )
        },
        topBarState = topAppBarState,
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
                visible = state.viewModelState == ViewModelState.Editing && !keyboardState.value,
                enter = floatingActionButtonEnterAnimation(),
                exit = floatingActionButtonExitAnimation()
            ) {
                BasicFloatingActionButton(icon = Icons.Rounded.Add) {
                    sendIntent(ShopsIntent.NavigateToShop(ShopArgs()))
                }
            }

            AnimatedVisibility(visible = !keyboardState.value) {
                BasicFloatingActionButton(
                    icon = when (state.viewModelState) {
                        ViewModelState.Editing -> Icons.Rounded.EditOff
                        ViewModelState.Viewing -> Icons.Rounded.Edit
                    },
                    onClick = { sendIntent(ShopsIntent.SwitchEdit) }
                )
            }
        },
        fabModifier = Modifier
            .windowInsetsPadding(WindowInsets.tappableElement.only(WindowInsetsSides.End))
            .padding(bottom = contentPadding.calculateBottomPadding())
            .onGloballyPositioned { fabHeightPx.floatValue = it.size.height.toFloat() },
        contentWindowInsets = WindowInsets.ime.only(WindowInsetsSides.Bottom),
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
    ) {
        ShopsList(
            state = state,
            contentState = lazyListState,
            contentPadding = contentPadding.copy(LocalLayoutDirection.current) {
                copy(
                    bottom = with(LocalDensity.current) {
                        (bottom + fabHeightPx.floatValue.toDp()).animate()
                    }
                )
            },
            sendIntent = sendIntent
        )
    }
}



@Composable
private fun ShopsList(
    state: ShopsState,
    contentState: LazyListState,
    contentPadding: PaddingValues,
    sendIntent: (ShopsIntent) -> Unit
) {
    Crossfade(
        targetState = state.shops?.toList().toListState(),
        label = "loading animation",
        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
        modifier = Modifier.fillMaxSize()
    ) { listState ->
        when (listState) {
            is ListState.Loading -> LoadingInBox(Modifier.padding(contentPadding))

            is ListState.Empty -> {
                EmptyList(
                    text = when (val appBarState = state.appBarState) {
                        is HomeTopAppBarState.Search -> {
                            when {
                                appBarState.query.isBlank() -> stringResource(R.string.empty_search_query)
                                else -> stringResource(R.string.empty_search_results)
                            }
                        }

                        is HomeTopAppBarState.TopBar -> {
                            when (state.viewModelState) {
                                ViewModelState.Viewing -> stringResource(R.string.empty_shops_list_viewing)
                                ViewModelState.Editing -> stringResource(R.string.empty_shops_list_editing)
                            }
                        }
                    },
                    icon = Icons.Rounded.DataArray,
                    iconModifier = Modifier.scale(2.5f),
                    modifier = Modifier
                        .padding(contentPadding)
                        .fillMaxSize()
                )
            }

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

                itemsIndexed(listState.data) { index, (shop, maxCashbacks) ->
                    MaxCashbackOwnerComposable(
                        cashbackOwner = shop.asCashbackOwner(),
                        maxCashbacks = maxCashbacks.toImmutableSet(),
                        isEditing = state.viewModelState == ViewModelState.Editing,
                        isSwiped = state.selectedShopIndex == index,
                        onSwipe = { isSwiped ->
                            sendIntent(ShopsIntent.SwipeShop(index, isSwiped))
                        },
                        onClick = {
                            sendIntent(ShopsIntent.SwipeShop(null))
                            sendIntent(
                                ShopsIntent.NavigateToShop(
                                    ShopArgs(
                                        shop.id,
                                        isEditing = false
                                    )
                                )
                            )
                        },
                        onClickToCashback = { cashback ->
                            sendIntent(
                                ShopsIntent.NavigateToCashback(
                                    CashbackArgs.fromShop(
                                        cashbackId = cashback.id,
                                        shopId = shop.id
                                    )
                                )
                            )
                        },
                        onEdit = {
                            sendIntent(
                                ShopsIntent.SwipeShop(position = null)
                            )
                            sendIntent(
                                ShopsIntent.NavigateToShop(
                                    ShopArgs(
                                        shop.id,
                                        isEditing = true
                                    )
                                )
                            )
                        },
                        onDelete = {
                            sendIntent(ShopsIntent.SwipeShop(position = null))
                            sendIntent(
                                ShopsIntent.OpenDialog(DialogType.ConfirmDeletion(shop))
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