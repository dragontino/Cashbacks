package com.cashbacks.app.ui.features.home.shops

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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cashbacks.app.ui.composables.BasicFloatingActionButton
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.ConfirmDeletionDialog
import com.cashbacks.app.ui.composables.EmptyList
import com.cashbacks.app.ui.composables.MaxCashbackOwnerComposable
import com.cashbacks.app.ui.features.home.HomeTopAppBar
import com.cashbacks.app.ui.features.home.HomeTopAppBarState
import com.cashbacks.app.ui.features.home.shops.mvi.ShopsAction
import com.cashbacks.app.ui.features.home.shops.mvi.ShopsEvent
import com.cashbacks.app.ui.features.shop.ShopArgs
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ListState
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.floatingActionButtonEnterAnimation
import com.cashbacks.app.util.floatingActionButtonExitAnimation
import com.cashbacks.app.util.keyboardAsState
import com.cashbacks.app.util.reversed
import com.cashbacks.domain.R
import com.cashbacks.domain.model.BasicCategoryShop
import com.cashbacks.domain.model.Shop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun ShopsScreen(
    viewModel: ShopsViewModel,
    title: String,
    bottomPadding: Dp,
    openDrawer: () -> Unit,
    navigateToShop: (args: ShopArgs) -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        when (viewModel.viewModelState) {
            ViewModelState.Editing -> viewModel.push(ShopsAction.FinishEdit)
            ViewModelState.Viewing -> viewModel.push(ShopsAction.ClickButtonBack)
        }
    }

    val topAppBarState = rememberTopAppBarState()
    val lazyListState = rememberLazyListState()
    val snackbarHostState = remember(::SnackbarHostState)
    val keyboardState = keyboardAsState()
    val dialogType = rememberSaveable { mutableStateOf<DialogType?>(null) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.onEach { event ->
            when (event) {
                is ShopsEvent.OpenDialog -> dialogType.value = event.type
                is ShopsEvent.CloseDialog -> dialogType.value = null
                is ShopsEvent.NavigateBack -> navigateBack()
                is ShopsEvent.NavigateToShop -> navigateToShop(event.args)
                is ShopsEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }.launchIn(this)
    }

    dialogType.value.takeIf { it is DialogType.ConfirmDeletion<*> }?.let { type ->
        val shop = (type as DialogType.ConfirmDeletion<*>).value as Shop
        ConfirmDeletionDialog(
            text = stringResource(R.string.confirm_shop_deletion, shop.name),
            onConfirm = { viewModel.push(ShopsAction.DeleteShop(shop)) },
            onClose = { viewModel.push(ShopsAction.CloseDialog) }
        )
    }

    val fabHeightPx = rememberSaveable { mutableFloatStateOf(0f) }

    CollapsingToolbarScaffold(
        topBar = {
            HomeTopAppBar(
                title = title,
                state = viewModel.appBarState,
                onStateChange = {
                    viewModel.push(ShopsAction.UpdateAppBarState(it))
                },
                searchPlaceholder = stringResource(R.string.search_shops_placeholder),
                onNavigationIconClick = openDrawer
            )
        },
        topBarState = topAppBarState,
        contentState = lazyListState,
        topBarContainerColor = when (viewModel.appBarState) {
            HomeTopAppBarState.Search -> Color.Unspecified
            HomeTopAppBarState.TopBar -> MaterialTheme.colorScheme.primary
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    containerColor = MaterialTheme.colorScheme.background.reversed.animate(),
                    contentColor = MaterialTheme.colorScheme.onBackground.reversed.animate(),
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        floatingActionButtons = {
            AnimatedVisibility(
                visible = viewModel.viewModelState == ViewModelState.Editing && !keyboardState.value,
                enter = floatingActionButtonEnterAnimation(),
                exit = floatingActionButtonExitAnimation()
            ) {
                BasicFloatingActionButton(icon = Icons.Rounded.Add) {
                    viewModel.push(ShopsAction.NavigateToShop(ShopArgs()))
                }
            }

            AnimatedVisibility(visible = !keyboardState.value) {
                BasicFloatingActionButton(
                    icon = when (viewModel.viewModelState) {
                        ViewModelState.Editing -> Icons.Rounded.EditOff
                        ViewModelState.Viewing -> Icons.Rounded.Edit
                    },
                    onClick = {
                        viewModel.onItemClick {
                            viewModel.push(ShopsAction.SwitchEdit)
                        }
                    }
                )
            }
        },
        fabModifier = Modifier
            .windowInsetsPadding(WindowInsets.tappableElement.only(WindowInsetsSides.End))
            .padding(bottom = bottomPadding)
            .onGloballyPositioned { fabHeightPx.floatValue = it.size.height.toFloat() },
        contentWindowInsets = WindowInsets.ime.only(WindowInsetsSides.Bottom),
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
    ) {

        val shopsState = viewModel.shopsFlow.collectAsStateWithLifecycle()
        Crossfade(
            targetState = ListState.fromList(shopsState.value),
            label = "loading animation",
            animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
            modifier = Modifier.fillMaxSize()
        ) { listState ->
            when (listState) {
                is ListState.Loading -> {
                    LoadingInBox(modifier = Modifier.padding(bottom = bottomPadding))
                }
                is ListState.Empty -> {
                    EmptyList(
                        text = when (val appBarState = viewModel.appBarState) {
                            is HomeTopAppBarState.Search -> {
                                when {
                                    appBarState.query.isBlank() -> stringResource(R.string.empty_search_query)
                                    else -> stringResource(R.string.empty_search_results)
                                }
                            }

                            is HomeTopAppBarState.TopBar -> {
                                when (viewModel.viewModelState) {
                                    ViewModelState.Viewing -> stringResource(R.string.empty_shops_list_viewing)
                                    ViewModelState.Editing -> stringResource(R.string.empty_shops_list_editing)
                                }
                            }
                        },
                        icon = Icons.Rounded.DataArray,
                        iconModifier = Modifier.scale(2.5f),
                        modifier = Modifier
                            .padding(bottom = bottomPadding)
                            .fillMaxSize()
                    )
                }

                is ListState.Stable -> {
                    ShopsList(
                        shopList = listState.data,
                        viewModel = viewModel,
                        state = lazyListState,
                        bottomPadding = with(LocalDensity.current) {
                            (bottomPadding + fabHeightPx.floatValue.toDp()).animate()
                        }
                    )
                }
            }
        }
    }
}



@Composable
private fun ShopsList(
    shopList: List<BasicCategoryShop>,
    viewModel: ShopsViewModel,
    state: LazyListState,
    bottomPadding: Dp
) {
    LazyColumn(
        state = state,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(shopList) { index, shop ->
            MaxCashbackOwnerComposable(
                cashbackOwner = shop,
                isEditing = viewModel.viewModelState == ViewModelState.Editing,
                isSwiped = viewModel.selectedShopIndex == index,
                onSwipe = { isSwiped ->
                    viewModel.push(ShopsAction.SwipeShop(isSwiped, index))
                },
                onClick = {
                    viewModel.onItemClick {
                        viewModel.push(
                            ShopsAction.SwipeShop(isOpened = false)
                        )
                        viewModel.push(
                            ShopsAction.NavigateToShop(ShopArgs(shop.id, isEditing = false))
                        )
                    }
                },
                onEdit = {
                    viewModel.onItemClick {
                        viewModel.push(
                            ShopsAction.SwipeShop(isOpened = false)
                        )
                        viewModel.push(
                            ShopsAction.NavigateToShop(ShopArgs(shop.id, isEditing = true))
                        )
                    }
                },
                onDelete = {
                    viewModel.onItemClick {
                        viewModel.push(ShopsAction.SwipeShop(isOpened = false))
                        viewModel.push(
                            ShopsAction.OpenDialog(DialogType.ConfirmDeletion(shop))
                        )
                    }
                }
            )
        }

        item {
            Spacer(Modifier.height(bottomPadding))
        }
    }
}