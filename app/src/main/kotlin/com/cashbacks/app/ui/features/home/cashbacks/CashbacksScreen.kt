package com.cashbacks.app.ui.features.home.cashbacks

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
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DataArray
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
import com.cashbacks.app.ui.composables.CashbackComposable
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.ConfirmDeletionDialog
import com.cashbacks.app.ui.composables.EmptyList
import com.cashbacks.app.ui.composables.ModalBottomSheet
import com.cashbacks.app.ui.composables.ModalSheetItems.IconTextItem
import com.cashbacks.app.ui.features.cashback.CashbackArgs
import com.cashbacks.app.ui.features.home.HomeTopAppBar
import com.cashbacks.app.ui.features.home.HomeTopAppBarState
import com.cashbacks.app.ui.features.home.cashbacks.mvi.CashbacksAction
import com.cashbacks.app.ui.features.home.cashbacks.mvi.CashbacksEvent
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ListState
import com.cashbacks.app.ui.managment.ScreenState
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.floatingActionButtonEnterAnimation
import com.cashbacks.app.util.floatingActionButtonExitAnimation
import com.cashbacks.app.util.keyboardAsState
import com.cashbacks.app.util.reversed
import com.cashbacks.domain.R
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.FullCashback
import com.cashbacks.domain.model.Shop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CashbacksScreen(
    viewModel: CashbacksViewModel,
    title: String,
    openDrawer: () -> Unit,
    navigateToCashback: (args: CashbackArgs) -> Unit,
    navigateBack: () -> Unit,
    bottomPadding: Dp,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.push(CashbacksAction.ClickButtonBack)
    }

    val snackbarHostState = remember(::SnackbarHostState)
    val dialogType = rememberSaveable { mutableStateOf<DialogType?>(null) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is CashbacksEvent.NavigateBack -> navigateBack()
                is CashbacksEvent.NavigateToCashback -> navigateToCashback(event.args)
                is CashbacksEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is CashbacksEvent.OpenDialog -> dialogType.value = event.type
                is CashbacksEvent.CloseDialog -> dialogType.value = null
                is CashbacksEvent.OpenNavigationDrawer -> openDrawer()
            }
        }
    }

    dialogType.value?.takeIf { it is DialogType.ConfirmDeletion<*> }?.let { type ->
        val cashback = (type as DialogType.ConfirmDeletion<*>).value as Cashback
        ConfirmDeletionDialog(
            text = stringResource(R.string.confirm_cashback_deletion),
            onConfirm = { viewModel.push(CashbacksAction.DeleteCashback(cashback)) },
            onClose = { viewModel.push(CashbacksAction.CloseDialog) }
        )
    }


    if (viewModel.showBottomSheet) {
        ModalBottomSheet(
            onClose = { viewModel.push(CashbacksAction.CloseBottomSheet) },
            title = stringResource(R.string.add_cashback_title),
            beautifulDesign = true
        ) {
            IconTextItem(
                icon = Icons.Outlined.Category,
                iconTintColor = MaterialTheme.colorScheme.primary,
                text = stringResource(R.string.to_category)
            ) {
                viewModel.onItemClick {
                    viewModel.push(
                        CashbacksAction.NavigateToCashback(
                            args = CashbackArgs.fromCategory(categoryId = null)
                        )
                    )
                    viewModel.push(CashbacksAction.CloseBottomSheet)
                }
            }

            IconTextItem(
                icon = Icons.Outlined.Store,
                iconTintColor = MaterialTheme.colorScheme.primary,
                text = stringResource(R.string.to_shop)
            ) {
                viewModel.onItemClick {
                    viewModel.push(
                        CashbacksAction.NavigateToCashback(
                            args = CashbackArgs.fromShop(shopId = null)
                        )
                    )
                    viewModel.push(CashbacksAction.CloseBottomSheet)
                }
            }
        }
    }

    Crossfade(
        targetState = viewModel.state,
        label = "cashbacks screen loading animation",
        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
    ) { state ->
        when (state) {
            ScreenState.Loading -> {
                LoadingInBox(
                    modifier = Modifier.padding(bottom = bottomPadding)
                )
            }

            ScreenState.Showing -> {
                CashbacksScreenContent(
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState,
                    title = title,
                    bottomPadding = bottomPadding,
                    modifier = modifier
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CashbacksScreenContent(
    viewModel: CashbacksViewModel,
    snackbarHostState: SnackbarHostState,
    title: String,
    bottomPadding: Dp,
    modifier: Modifier = Modifier
) {
    val topBarState = rememberTopAppBarState()
    val lazyListState = rememberLazyListState()
    val keyboardState = keyboardAsState()
    val fabHeightPx = rememberSaveable { mutableFloatStateOf(0f) }

    CollapsingToolbarScaffold(
        topBar = {
            HomeTopAppBar(
                title = title,
                state = viewModel.appBarState,
                onStateChange = {
                    viewModel.push(CashbacksAction.UpdateAppBarState(it))
                },
                searchPlaceholder = stringResource(R.string.search_cashbacks_placeholder),
                onNavigationIconClick = {
                    viewModel.push(CashbacksAction.ClickNavigationButton)
                }
            )
        },
        topBarState = topBarState,
        topBarScrollEnabled = viewModel.appBarState is HomeTopAppBarState.TopBar,
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
                visible = !keyboardState.value,
                enter = floatingActionButtonEnterAnimation(),
                exit = floatingActionButtonExitAnimation()
            ) {
                BasicFloatingActionButton(icon = Icons.Rounded.Add) {
                    viewModel.push(CashbacksAction.OpenBottomSheet)
                }
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

        val cashbacks = viewModel.cashbacksFlow.collectAsStateWithLifecycle()
        Crossfade(
            targetState = ListState.fromList(cashbacks.value),
            label = "loading anim",
            animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
            modifier = Modifier.fillMaxSize()
        ) { listState ->
            when (listState) {
                is  ListState.Loading -> {
                    LoadingInBox(
                        modifier = Modifier.padding(bottom = bottomPadding)
                    )
                }

                is ListState.Empty -> {
                    EmptyList(
                        text = when (val appBarState = viewModel.appBarState) {
                            is HomeTopAppBarState.Search -> {
                                if (appBarState.query.isBlank()) {
                                    stringResource(R.string.empty_search_query)
                                } else {
                                    stringResource(R.string.empty_search_results)
                                }
                            }

                            else -> stringResource(R.string.no_cashbacks)
                        },
                        icon = Icons.Rounded.DataArray,
                        iconModifier = Modifier.scale(2.5f),
                        modifier = Modifier
                            .padding(bottom = bottomPadding)
                            .fillMaxSize()
                    )
                }

                is ListState.Stable -> {
                    CashbacksList(
                        viewModel = viewModel,
                        cashbackList = listState.data,
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
private fun CashbacksList(
    viewModel: CashbacksViewModel,
    cashbackList: List<FullCashback>,
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
        itemsIndexed(cashbackList) { index, cashback ->
            CashbackComposable(
                cashback = cashback,
                isSwiped = viewModel.selectedCashbackIndex == index,
                onSwipe = { isSwiped ->
                    viewModel.push(CashbacksAction.SwipeCashback(isSwiped, index))
                },
                onClick = {
                    viewModel.onItemClick {
                        val args = when (cashback.owner) {
                            is Category -> CashbackArgs.fromCategory(
                                categoryId = cashback.owner.id,
                                cashbackId = cashback.id
                            )
                            is Shop -> CashbackArgs.fromShop(
                                cashbackId = cashback.id,
                                shopId = cashback.owner.id
                            )
                        }
                        viewModel.push(CashbacksAction.NavigateToCashback(args))
                    }
                },
                onDelete = {
                    viewModel.onItemClick {
                        viewModel.push(CashbacksAction.SwipeCashback(isOpened = false))
                        viewModel.push(
                            CashbacksAction.OpenDialog(DialogType.ConfirmDeletion(cashback))
                        )
                    }
                }
            )
        }

        item {
            Spacer(
                modifier = Modifier.height(bottomPadding)
            )
        }
    }
}