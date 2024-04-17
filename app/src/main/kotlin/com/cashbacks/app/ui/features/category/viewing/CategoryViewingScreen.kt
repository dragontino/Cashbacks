package com.cashbacks.app.ui.features.category.viewing

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cashbacks.domain.R
import com.cashbacks.app.ui.composables.BasicFloatingActionButton
import com.cashbacks.app.ui.composables.CashbackComposable
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffoldDefaults
import com.cashbacks.app.ui.composables.ConfirmDeletionDialog
import com.cashbacks.app.ui.composables.ListContentTabPage
import com.cashbacks.app.ui.composables.OnLifecycleEvent
import com.cashbacks.app.ui.composables.PrimaryTabsLayout
import com.cashbacks.app.ui.composables.ShopComposable
import com.cashbacks.app.ui.features.cashback.CashbackArgs
import com.cashbacks.app.ui.features.category.CategoryArgs
import com.cashbacks.app.ui.features.category.TabItem
import com.cashbacks.app.ui.features.shop.ShopArgs
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ScreenEvents
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.util.Loading
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Shop
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun CategoryViewingScreen(
    viewModel: CategoryViewingViewModel,
    startTab: TabItem,
    navigateToCategory: (args: CategoryArgs) -> Unit,
    navigateToShop: (args: ShopArgs) -> Unit,
    navigateToCashback: (args: CashbackArgs) -> Unit,
    popBackStack: () -> Unit
) {
    BackHandler(onBack = popBackStack)

    val tabItems = TabItem.entries
    val pagerState = rememberPagerState(initialPage = tabItems.indexOf(startTab)) { tabItems.size }
    val snackbarState = remember(::SnackbarHostState)
    val scope = rememberCoroutineScope()

    val currentScreen = remember(pagerState.currentPage) {
        derivedStateOf { tabItems[pagerState.currentPage] }
    }

    val showSnackbar = remember {
        fun(message: String) {
            scope.launch { if (message.isNotBlank()) snackbarState.showSnackbar(message) }
        }
    }

    OnLifecycleEvent(onStart = viewModel::onScreenLoading)

    var dialogType: DialogType? by rememberSaveable { mutableStateOf(null) }
    LaunchedEffect(key1 = true) {
        viewModel.eventsFlow.collect { event ->
            when (event) {
                is ScreenEvents.Navigate -> {
                    when (event.args) {
                        is CategoryArgs -> navigateToCategory(event.args)
                        is ShopArgs -> navigateToShop(event.args)
                        is CashbackArgs -> navigateToCashback(event.args)
                        null -> popBackStack()
                    }
                }
                is ScreenEvents.ShowSnackbar -> showSnackbar(event.message)
                is ScreenEvents.OpenDialog -> dialogType = event.type
                ScreenEvents.CloseDialog -> dialogType = null
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
                    is Shop -> viewModel.deleteShop(value)
                    is Cashback -> viewModel.deleteCashback(value)
                }
            },
            onClose = viewModel::closeDialog
        )
    }


    CollapsingToolbarScaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    when (viewModel.state.value) {
                        ViewModelState.Loading -> Loading(
                            color = MaterialTheme.colorScheme.onPrimary.animate(),
                            modifier = Modifier.scale(.6f)
                        )
                        else -> Text(
                            text = viewModel.category.value.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = popBackStack) {
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
        floatingActionButtons = {
            BasicFloatingActionButton(
                icon = Icons.Rounded.Edit,
                onClick = {
                    viewModel.onItemClick {
                        viewModel.navigateTo(
                            args = CategoryArgs(
                                id = viewModel.categoryId,
                                isEditing = true,
                                startTab = currentScreen.value
                            )
                        )
                    }
                }
            )
        },
        fabModifier = Modifier
            .graphicsLayer { viewModel.fabPaddingDp.floatValue = size.height.toDp().value }
            .windowInsetsPadding(CollapsingToolbarScaffoldDefaults.contentWindowInsets),
        snackbarHost = {
            SnackbarHost(hostState = snackbarState) {
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
            targetState = viewModel.state.value,
            label = "loading anim",
            animationSpec = tween(durationMillis = 100, easing = LinearEasing),
            modifier = Modifier.fillMaxSize()
        ) { state ->
            when (state) {
                ViewModelState.Loading -> LoadingInBox()
                else -> CategoryViewerContent(viewModel, pagerState, tabItems)
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryViewerContent(
    viewModel: CategoryViewingViewModel,
    pagerState: PagerState,
    tabPages: List<TabItem>,
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
            pages = tabPages
        ) { _, page ->
            ListContentTabPage(
                items = when (page) {
                    TabItem.Shops -> viewModel.shopsLiveData.observeAsState().value
                    TabItem.Cashbacks -> viewModel.cashbacksLiveData.observeAsState().value
                },
                placeholderText = when (page) {
                    TabItem.Shops -> stringResource(R.string.empty_shops_list_viewing)
                    TabItem.Cashbacks -> stringResource(R.string.empty_cashbacks_list)
                },
                bottomSpacing = viewModel.fabPaddingDp.floatValue.dp,
                modifier = Modifier.padding(8.dp)
            ) { index, item ->
                when (item) {
                    is Shop -> ShopComposable(
                        shop = item,
                        isEditing = false,
                        isSwiped = viewModel.selectedShopIndex == index,
                        onSwipe = { isSwiped ->
                            viewModel.selectedShopIndex = when {
                                isSwiped -> index
                                else -> null
                            }
                        },
                        onClick = {
                            viewModel.onItemClick {
                                viewModel.selectedShopIndex = -1
                                viewModel.navigateTo(
                                    args = ShopArgs(shopId = item.id, isEditing = false)
                                )
                            }
                        },
                        onEdit = {
                            viewModel.onItemClick {
                                viewModel.selectedShopIndex = -1
                                viewModel.navigateTo(
                                    args = ShopArgs(shopId = item.id, isEditing = true)
                                )
                            }
                        },
                        onDelete = {
                            viewModel.onItemClick {
                                viewModel.selectedShopIndex = -1
                                viewModel.openDialog(DialogType.ConfirmDeletion(item))
                            }
                        }
                    )

                    is Cashback -> CashbackComposable(
                        cashback = item,
                        isSwiped = viewModel.selectedCashbackIndex == index,
                        onSwipe = { isSwiped ->
                            viewModel.selectedCashbackIndex = when {
                                isSwiped -> index
                                else -> null
                            }
                        },
                        onClick = {
                            viewModel.onItemClick {
                                viewModel.selectedCashbackIndex = -1
                                viewModel.navigateTo(CashbackArgs.Existing(item.id))
                            }
                        },
                        onDelete = {
                            viewModel.onItemClick {
                                viewModel.selectedCashbackIndex = -1
                                viewModel.openDialog(DialogType.ConfirmDeletion(item))
                            }
                        }
                    )
                }
            }
        }
    }
}
