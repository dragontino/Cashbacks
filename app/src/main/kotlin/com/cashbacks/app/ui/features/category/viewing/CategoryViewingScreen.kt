package com.cashbacks.app.ui.features.category.viewing

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
import com.cashbacks.app.ui.composables.BasicFloatingActionButton
import com.cashbacks.app.ui.composables.CashbackComposable
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffoldDefaults
import com.cashbacks.app.ui.composables.ConfirmDeletionDialog
import com.cashbacks.app.ui.composables.ListContentTabPage
import com.cashbacks.app.ui.composables.MaxCashbackOwnerComposable
import com.cashbacks.app.ui.composables.PrimaryTabsLayout
import com.cashbacks.app.ui.features.cashback.CashbackArgs
import com.cashbacks.app.ui.features.category.CategoryArgs
import com.cashbacks.app.ui.features.category.CategoryTabItem
import com.cashbacks.app.ui.features.category.CategoryTabItemType
import com.cashbacks.app.ui.features.category.mvi.CategoryAction
import com.cashbacks.app.ui.features.category.mvi.CategoryEvent
import com.cashbacks.app.ui.features.shop.ShopArgs
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ListState
import com.cashbacks.app.ui.managment.ScreenState
import com.cashbacks.app.util.Loading
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.domain.R
import com.cashbacks.domain.model.BasicCashback
import com.cashbacks.domain.model.BasicShop
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Shop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CategoryViewingScreen(
    viewModel: CategoryViewingViewModel,
    startTab: CategoryTabItemType,
    navigateToCategory: (args: CategoryArgs) -> Unit,
    navigateToShop: (args: ShopArgs) -> Unit,
    navigateToCashback: (args: CashbackArgs) -> Unit,
    navigateBack: () -> Unit
) {
    BackHandler(onBack = navigateBack)

    val tabItems = listOf(CategoryTabItem.Cashbacks, CategoryTabItem.Shops)
    val pagerState = rememberPagerState(
        initialPage = tabItems.indexOfFirst { it.type == startTab },
        pageCount = { tabItems.size }
    )
    val snackbarState = remember(::SnackbarHostState)

    val currentScreen = remember(pagerState.currentPage) {
        derivedStateOf { tabItems[pagerState.currentPage] }
    }
    var dialogType: DialogType? by rememberSaveable { mutableStateOf(null) }
    val fabHeightDp = rememberSaveable { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is CategoryEvent.ShowSnackbar -> snackbarState.showSnackbar(event.message)
                is CategoryEvent.OpenDialog -> dialogType = event.type
                is CategoryEvent.CloseDialog -> dialogType = null
                is CategoryEvent.NavigateBack -> navigateBack()
                is CategoryEvent.NavigateToCategoryEditingScreen -> navigateToCategory(event.args)
                is CategoryEvent.NavigateToShopScreen -> navigateToShop(event.args)
                is CategoryEvent.NavigateToCashbackScreen -> navigateToCashback(event.args)
                else -> {}
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
                    is BasicShop -> viewModel.push(CategoryAction.DeleteShop(value))
                    is BasicCashback -> viewModel.push(CategoryAction.DeleteCashback(value))
                }
            },
            onClose = { viewModel.push(CategoryAction.CloseDialog) }
        )
    }


    CollapsingToolbarScaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    when (viewModel.state) {
                        ScreenState.Loading -> Loading(
                            color = MaterialTheme.colorScheme.onPrimary.animate(),
                            modifier = Modifier.scale(.6f)
                        )
                        ScreenState.Showing -> Text(
                            text = viewModel.category.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
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
                        viewModel.push(
                            CategoryAction.NavigateToCategoryEditing(
                                startTab = currentScreen.value.type
                            )
                        )
                    }
                }
            )
        },
        fabModifier = Modifier
            .graphicsLayer { fabHeightDp.floatValue = size.height.toDp().value }
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
            targetState = viewModel.state,
            label = "loading anim",
            animationSpec = tween(durationMillis = 100, easing = LinearEasing),
            modifier = Modifier.fillMaxSize()
        ) { state ->
            when (state) {
                ScreenState.Loading -> LoadingInBox()
                ScreenState.Showing -> CategoryViewerContent(
                    viewModel = viewModel,
                    pagerState = pagerState,
                    tabPages = tabItems,
                    bottomPadding = fabHeightDp.floatValue.dp
                )
            }
        }
    }
}


@Composable
private fun CategoryViewerContent(
    viewModel: CategoryViewingViewModel,
    pagerState: PagerState,
    tabPages: List<CategoryTabItem>,
    bottomPadding: Dp,
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
                contentState = ListState.fromList(
                    when (page) {
                        CategoryTabItem.Shops -> viewModel.category.shops
                        CategoryTabItem.Cashbacks -> viewModel.category.cashbacks
                    }
                ),
                placeholderText = when (page) {
                    CategoryTabItem.Cashbacks -> stringResource(R.string.empty_cashbacks_list_editing)
                    CategoryTabItem.Shops -> stringResource(R.string.empty_shops_list_viewing)
                },
                bottomSpacing = bottomPadding,
                modifier = Modifier.padding(8.dp)
            ) { index, item ->
                when (item) {
                    is BasicShop -> MaxCashbackOwnerComposable(
                        cashbackOwner = item,
                        isEditing = false,
                        isSwiped = viewModel.selectedShopIndex == index,
                        onSwipe = { isSwiped ->
                            viewModel.push(CategoryAction.SwipeShop(index, isSwiped))
                        },
                        onClick = {
                            viewModel.onItemClick {
                                viewModel.push(CategoryAction.SwipeShop(index, false))
                                viewModel.push(
                                    CategoryAction.NavigateToShop(
                                        ShopArgs(item.id, isEditing = false)
                                    )
                                )
                            }
                        },
                        onEdit = {
                            viewModel.onItemClick {
                                viewModel.push(CategoryAction.SwipeShop(index, false))
                                viewModel.push(
                                    CategoryAction.NavigateToShop(
                                        ShopArgs(item.id, isEditing = true)
                                    )
                                )
                            }
                        },
                        onDelete = {
                            viewModel.onItemClick {
                                viewModel.push(CategoryAction.SwipeShop(index, false))
                                viewModel.push(
                                    CategoryAction.OpenDialog(DialogType.ConfirmDeletion(item))
                                )
                            }
                        }
                    )

                    is BasicCashback -> CashbackComposable(
                        cashback = item,
                        isSwiped = viewModel.selectedCashbackIndex == index,
                        onSwipe = { isSwiped ->
                            viewModel.push(CategoryAction.SwipeCashback(index, isSwiped))
                        },
                        onClick = {
                            viewModel.onItemClick {
                                viewModel.push(CategoryAction.SwipeCashback(index, false))
                                viewModel.push(CategoryAction.NavigateToCashback(item.id))
                            }
                        },
                        onDelete = {
                            viewModel.onItemClick {
                                viewModel.push(CategoryAction.SwipeCashback(index, false))
                                viewModel.push(
                                    CategoryAction.OpenDialog(DialogType.ConfirmDeletion(item))
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
