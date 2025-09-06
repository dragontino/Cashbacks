package com.cashbacks.features.category.presentation.impl.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cashbacks.common.composables.BasicFloatingActionButton
import com.cashbacks.common.composables.CollapsingToolbarScaffold
import com.cashbacks.common.composables.CollapsingToolbarScaffoldDefaults
import com.cashbacks.common.composables.ConfirmDeletionDialog
import com.cashbacks.common.composables.ListContentTabPage
import com.cashbacks.common.composables.Loading
import com.cashbacks.common.composables.LoadingInBox
import com.cashbacks.common.composables.PrimaryTabsLayout
import com.cashbacks.common.composables.management.DialogType
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.common.composables.management.toListState
import com.cashbacks.common.composables.utils.animate
import com.cashbacks.common.resources.R
import com.cashbacks.common.utils.mvi.IntentSender
import com.cashbacks.features.cashback.domain.model.BasicCashback
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.presentation.api.CashbackArgs
import com.cashbacks.features.cashback.presentation.api.composables.CashbackComposable
import com.cashbacks.features.cashback.presentation.api.composables.MaxCashbackOwnerComposable
import com.cashbacks.features.category.presentation.api.CategoryArgs
import com.cashbacks.features.category.presentation.impl.CategoryTabItem
import com.cashbacks.features.category.presentation.impl.mvi.CategoryIntent
import com.cashbacks.features.category.presentation.impl.mvi.CategoryLabel
import com.cashbacks.features.category.presentation.impl.mvi.CategoryViewingState
import com.cashbacks.features.category.presentation.impl.mvi.ShopWithCashback
import com.cashbacks.features.category.presentation.impl.mvi.ViewingIntent
import com.cashbacks.features.category.presentation.impl.mvi.ViewingLabel
import com.cashbacks.features.category.presentation.impl.tabItems
import com.cashbacks.features.category.presentation.impl.viewmodel.CategoryViewingViewModel
import com.cashbacks.features.shop.domain.model.Shop
import com.cashbacks.features.shop.presentation.api.ShopArgs
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun CategoryViewingRoot(
    navigateToCategory: (args: CategoryArgs) -> Unit,
    navigateToShop: (args: ShopArgs) -> Unit,
    navigateToCashback: (args: CashbackArgs) -> Unit,
    navigateBack: () -> Unit,
    viewModel: CategoryViewingViewModel = koinViewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember(::SnackbarHostState)
    val pagerState = rememberPagerState(
        initialPage = tabItems.indexOfFirst { it.type == viewModel.startTab },
        pageCount = { tabItems.size }
    )
    var dialogType: DialogType? by rememberSaveable { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        viewModel.labelFlow.collect { label ->
            when (label) {
                is CategoryLabel.DisplayMessage -> launch {
                    snackbarHostState.showSnackbar(label.message)
                }
                is CategoryLabel.OpenDialog -> dialogType = label.type
                is CategoryLabel.CloseDialog -> dialogType = null
                is CategoryLabel.NavigateBack -> navigateBack()
                is ViewingLabel.NavigateToCategoryEditingScreen -> navigateToCategory(label.args)
                is CategoryLabel.NavigateToShopScreen -> navigateToShop(label.args)
                is CategoryLabel.NavigateToCashbackScreen -> navigateToCashback(label.args)
            }
        }
    }


    if (dialogType is DialogType.ConfirmDeletion<*>) {
        val value = (dialogType as DialogType.ConfirmDeletion<*>).value
        ConfirmDeletionDialog(
            text = when (value) {
                is Shop -> stringResource(R.string.confirm_shop_deletion, value.name)
                is Cashback -> stringResource(R.string.confirm_cashback_deletion)
                else -> ""
            },
            onConfirm = {
                when (value) {
                    is Shop -> viewModel.sendIntent(ViewingIntent.DeleteShop(value))
                    is Cashback -> viewModel.sendIntent(ViewingIntent.DeleteCashback(value))
                }
            },
            onClose = { viewModel.sendIntent(CategoryIntent.CloseDialog) }
        )
    }


    CategoryViewingScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        pagerState = pagerState,
        intentSender = IntentSender(viewModel::sendIntent)
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryViewingScreen(
    state: CategoryViewingState,
    snackbarHostState: SnackbarHostState,
    pagerState: PagerState,
    intentSender: IntentSender<ViewingIntent>
) {
    BackHandler {
        intentSender.sendWithDelay(CategoryIntent.ClickButtonBack)
    }

    val currentScreen = remember(pagerState.currentPage) {
        derivedStateOf { tabItems[pagerState.currentPage] }
    }
    val fabHeightDp = rememberSaveable { mutableFloatStateOf(0f) }

    CollapsingToolbarScaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    when (state.screenState) {
                        ScreenState.Loading -> Loading(
                            color = MaterialTheme.colorScheme.onPrimary.animate(),
                            modifier = Modifier.scale(.6f)
                        )
                        ScreenState.Stable -> Text(
                            text = state.category.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            intentSender.sendWithDelay(CategoryIntent.ClickButtonBack)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = "return to previous screen",
                            modifier = Modifier.scale(1.2f)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary.animate(),
                    titleContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary.animate()
                )
            )
        },
        topBarScrollEnabled = false,
        contentScrollEnabled = !pagerState.isScrollInProgress,
        floatingActionButtons = {
            BasicFloatingActionButton(
                icon = Icons.Rounded.Edit,
                onClick = {
                    intentSender.sendWithDelay(
                        ViewingIntent.NavigateToCategoryEditing(currentScreen.value.type)
                    )
                }
            )
        },
        fabModifier = Modifier
            .graphicsLayer { fabHeightDp.floatValue = size.height.toDp().value }
            .windowInsetsPadding(CollapsingToolbarScaffoldDefaults.contentWindowInsets),
        snackbarHost = {
            SnackbarHost(snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    containerColor = MaterialTheme.colorScheme.onBackground.animate(),
                    contentColor = MaterialTheme.colorScheme.background.animate(),
                    actionColor = MaterialTheme.colorScheme.primary.animate(),
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        contentWindowInsets = WindowInsets.ime.only(WindowInsetsSides.Bottom),
        modifier = Modifier.fillMaxSize()
    ) {
        Crossfade(
            targetState = state.screenState,
            label = "loading anim",
            animationSpec = tween(durationMillis = 100, easing = LinearEasing),
            modifier = Modifier.fillMaxSize()
        ) { screenState ->
            when (screenState) {
                ScreenState.Loading -> LoadingInBox()
                ScreenState.Stable -> CategoryViewingContent(
                    state = state,
                    pagerState = pagerState,
                    bottomPadding = fabHeightDp.floatValue.dp,
                    intentSender = intentSender
                )
            }
        }
    }
}


@Composable
private fun CategoryViewingContent(
    state: CategoryViewingState,
    pagerState: PagerState,
    bottomPadding: Dp,
    intentSender: IntentSender<ViewingIntent>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface.animate())
            .then(modifier)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PrimaryTabsLayout(
            pagerState = pagerState,
            pages = tabItems
        ) { _, page ->
            ListContentTabPage(
                contentState = when (page) {
                    CategoryTabItem.Shops -> state.shops
                    CategoryTabItem.Cashbacks -> state.cashbacks
                }.toListState(),
                placeholderText = when (page) {
                    CategoryTabItem.Cashbacks -> stringResource(R.string.empty_cashbacks_list_editing)
                    CategoryTabItem.Shops -> stringResource(R.string.empty_shops_list_viewing)
                },
                bottomSpacing = bottomPadding,
                modifier = Modifier.padding(8.dp)
            ) { index, item ->
                when (item) {
                    is ShopWithCashback -> MaxCashbackOwnerComposable(
                        mainContent = {
                            Text(
                                text = item.shop.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        maxCashback = item.maxCashback,
                        isEnabledToSwipe = state.swipedShopId in setOf(item.id, null),
                        onSwipeStatusChanged = { isOnSwipe ->
                            intentSender.sendWithDelay(
                                CategoryIntent.SwipeShop(item.id, isOnSwipe)
                            )
                        },
                        isExpanded = state.selectedShopId == item.id,
                        onExpandedStatusChanged = { expanded ->
                            intentSender.sendWithDelay(CategoryIntent.SelectShop(item.id, expanded))
                        },
                        onClick = {
                            intentSender.sendWithDelay(
                                ViewingIntent.NavigateToShop(item.shop.id)
                            )
                        },
                        onClickToCashback = {
                            intentSender.sendWithDelay(
                                ViewingIntent.NavigateToCashback(item.maxCashback!!.id)
                            )
                        },
                        onEdit = {
                            intentSender.sendWithDelay(
                                ViewingIntent.NavigateToShop(item.shop.id)
                            )
                        },
                        onDelete = {
                            intentSender.sendWithDelay(
                                CategoryIntent.OpenDialog(DialogType.ConfirmDeletion(item))
                            )
                        }
                    )

                    is BasicCashback -> CashbackComposable(
                        cashback = item,
                        isEnabledToSwipe = state.swipedCashbackId in setOf(item.id, null),
                        onSwipeStatusChanged = { isOnSwipe ->
                            intentSender.sendWithDelay(
                                CategoryIntent.SwipeCashback(item.id, isOnSwipe)
                            )
                        },
                        onClick = {
                            intentSender.sendWithDelay(ViewingIntent.NavigateToCashback(item.id))
                        },
                        onDelete = {
                            intentSender.sendWithDelay(
                                CategoryIntent.OpenDialog(DialogType.ConfirmDeletion(item))
                            )
                        }
                    )
                }
            }
        }
    }
}
