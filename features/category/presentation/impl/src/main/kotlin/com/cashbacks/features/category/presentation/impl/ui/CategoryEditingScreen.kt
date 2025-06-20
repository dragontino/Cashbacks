package com.cashbacks.features.category.presentation.impl.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Save
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cashbacks.common.composables.BasicFloatingActionButton
import com.cashbacks.common.composables.CollapsingToolbarScaffold
import com.cashbacks.common.composables.CollapsingToolbarScaffoldDefaults
import com.cashbacks.common.composables.ConfirmDeletionDialog
import com.cashbacks.common.composables.ConfirmExitWithSaveDataDialog
import com.cashbacks.common.composables.EditableTextField
import com.cashbacks.common.composables.ListContentTabPage
import com.cashbacks.common.composables.LoadingInBox
import com.cashbacks.common.composables.ModalSheetDefaults
import com.cashbacks.common.composables.NewNameTextField
import com.cashbacks.common.composables.OnLifecycleEvent
import com.cashbacks.common.composables.SecondaryTabsLayout
import com.cashbacks.common.composables.management.DialogType
import com.cashbacks.common.composables.management.ListState
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.common.composables.utils.animate
import com.cashbacks.common.composables.utils.floatingActionButtonEnterAnimation
import com.cashbacks.common.composables.utils.floatingActionButtonExitAnimation
import com.cashbacks.common.composables.utils.keyboardAsState
import com.cashbacks.common.resources.R
import com.cashbacks.features.cashback.domain.model.BasicCashback
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.domain.utils.asCashbackOwner
import com.cashbacks.features.cashback.presentation.api.CashbackArgs
import com.cashbacks.features.cashback.presentation.api.composables.CashbackComposable
import com.cashbacks.features.cashback.presentation.api.composables.MaxCashbackOwnerComposable
import com.cashbacks.features.category.domain.model.Category
import com.cashbacks.features.category.presentation.api.CategoryArgs
import com.cashbacks.features.category.presentation.impl.CategoryTabItem
import com.cashbacks.features.category.presentation.impl.mvi.CategoryEditingState
import com.cashbacks.features.category.presentation.impl.mvi.CategoryError
import com.cashbacks.features.category.presentation.impl.mvi.CategoryIntent
import com.cashbacks.features.category.presentation.impl.mvi.CategoryLabel
import com.cashbacks.features.category.presentation.impl.mvi.EditingIntent
import com.cashbacks.features.category.presentation.impl.mvi.EditingLabel
import com.cashbacks.features.category.presentation.impl.tabItems
import com.cashbacks.features.category.presentation.impl.viewmodel.CategoryEditingViewModel
import com.cashbacks.features.shop.domain.model.BasicShop
import com.cashbacks.features.shop.domain.model.Shop
import com.cashbacks.features.shop.presentation.api.ShopArgs
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@Composable
internal fun CategoryEditingRoot(
    navigateToCategory: (args: CategoryArgs) -> Unit,
    navigateToShop: (args: ShopArgs) -> Unit,
    navigateToCashback: (args: CashbackArgs) -> Unit,
    navigateBack: () -> Unit,
    viewModel: CategoryEditingViewModel = koinViewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember(::SnackbarHostState)
    val pagerState = rememberPagerState(
        initialPage = tabItems.indexOfFirst { it.type == viewModel.startTab },
        pageCount = { tabItems.size },
    )
    val openedDialogType = rememberSaveable { mutableStateOf<DialogType?>(null) }

    LaunchedEffect(Unit) {
        viewModel.labelFlow.collect { label ->
            when (label) {
                is CategoryLabel.OpenDialog -> openedDialogType.value = label.type
                is CategoryLabel.CloseDialog -> openedDialogType.value = null
                is CategoryLabel.DisplayMessage -> launch {
                    snackbarHostState.showSnackbar(label.message)
                }
                is CategoryLabel.NavigateBack -> navigateBack()
                is EditingLabel.NavigateToCategoryViewingScreen -> navigateToCategory(label.args)
                is CategoryLabel.NavigateToShopScreen -> navigateToShop(label.args)
                is CategoryLabel.NavigateToCashbackScreen -> navigateToCashback(label.args)
            }
        }
    }

    when (val type = openedDialogType.value) {
        is DialogType.ConfirmDeletion<*> -> {
            val value = type.value
            ConfirmDeletionDialog(
                text = when (value) {
                    is Category -> stringResource(
                        R.string.confirm_category_deletion,
                        state.category.name
                    )
                    is Shop -> stringResource(R.string.confirm_shop_deletion, value.name)
                    is Cashback -> stringResource(R.string.confirm_cashback_deletion)
                    else -> ""
                },
                onConfirm = {
                    when (value) {
                        is Category -> viewModel.sendIntent(EditingIntent.DeleteCategory())
                        is BasicShop -> viewModel.sendIntent(EditingIntent.DeleteShop(value))
                        is BasicCashback -> viewModel.sendIntent(EditingIntent.DeleteCashback(value))
                    }
                },
                onClose = { viewModel.sendIntent(CategoryIntent.CloseDialog) }
            )
        }
        DialogType.Save -> {
            ConfirmExitWithSaveDataDialog(
                onConfirm = {
                    viewModel.sendIntent(
                        EditingIntent.SaveCategory {
                            viewModel.sendIntent(CategoryIntent.ClickButtonBack)
                        }
                    )
                },
                onDismiss = { viewModel.sendIntent(CategoryIntent.ClickButtonBack) },
                onClose = { viewModel.sendIntent(CategoryIntent.CloseDialog) }
            )
        }
    }

    CategoryEditingScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        pagerState = pagerState,
        sendIntent = viewModel::sendIntent
    )
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryEditingScreen(
    state: CategoryEditingState,
    pagerState: PagerState,
    snackbarHostState: SnackbarHostState,
    sendIntent: (EditingIntent) -> Unit
) {
    BackHandler {
        when {
            state.isCategoryChanged() -> sendIntent(CategoryIntent.OpenDialog(DialogType.Save))
            else -> sendIntent(CategoryIntent.ClickButtonBack)
        }
    }

    OnLifecycleEvent(
        onDestroy = { sendIntent(EditingIntent.SaveCategory()) }
    )

    val keyboardIsOpen = keyboardAsState()

    LaunchedEffect(Unit) {
        snapshotFlow { keyboardIsOpen.value }.collect { isKeyboardOpen ->
            if (!isKeyboardOpen) {
                sendIntent(EditingIntent.FinishCreatingShop)
            }
        }
    }

    Box(
        modifier = Modifier
            .imePadding()
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CategoryEditingScreenContent(
            state = state,
            pagerState = pagerState,
            snackbarHostState = snackbarHostState,
            sendIntent = sendIntent
        )

        AnimatedVisibility(
            visible = state.screenState != ScreenState.Loading && state.isCreatingShop,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            ),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            NewNameTextField(placeholder = stringResource(R.string.shop_placeholder)) { name ->
                sendIntent(EditingIntent.SaveShop(name))
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CategoryEditingScreenContent(
    state: CategoryEditingState,
    pagerState: PagerState,
    snackbarHostState: SnackbarHostState,
    sendIntent: (EditingIntent) -> Unit
) {
    val fabPaddingDp = rememberSaveable { mutableFloatStateOf(0f) }
    val keyboardIsVisibleState = keyboardAsState()
    val listStates = List(tabItems.size) { rememberLazyListState() }

    val currentScreen = remember(pagerState.currentPage) {
        derivedStateOf { tabItems[pagerState.currentPage] }
    }
    val currentListState = remember(pagerState.currentPage) {
        listStates[pagerState.currentPage]
    }


    Crossfade(
        targetState = state.screenState,
        label = "category screen state animation",
        animationSpec = tween(durationMillis = 200, easing = LinearEasing)
    ) { screenState ->
        if (screenState == ScreenState.Loading) {
            LoadingInBox(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            )
        }
        else {
            CollapsingToolbarScaffold(
                contentState = currentListState,
                topBarScrollEnabled = false,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.category_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            Crossfade(
                                targetState = state.isCategoryChanged(),
                                label = "icon animation",
                                animationSpec = tween(
                                    durationMillis = 200,
                                    easing = LinearEasing
                                )
                            ) { isChanged ->
                                IconButton(
                                    onClick = {
                                        when {
                                            isChanged -> sendIntent(
                                                CategoryIntent.OpenDialog(DialogType.Save)
                                            )

                                            else -> sendIntent(CategoryIntent.ClickButtonBack)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = when {
                                            isChanged -> Icons.Rounded.Close
                                            else -> Icons.Rounded.ArrowBackIosNew
                                        },
                                        contentDescription = "return to previous screen",
                                        modifier = Modifier.scale(1.2f)
                                    )
                                }
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    val dialogType = DialogType.ConfirmDeletion(state.category)
                                    sendIntent(CategoryIntent.OpenDialog(dialogType))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "delete category",
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
                floatingActionButtons = {
                    AnimatedVisibility(
                        visible = !keyboardIsVisibleState.value,
                        enter = floatingActionButtonEnterAnimation(),
                        exit = floatingActionButtonExitAnimation()
                    ) {
                        BasicFloatingActionButton(icon = Icons.Rounded.Add) {
                            when (currentScreen.value) {
                                CategoryTabItem.Cashbacks -> sendIntent(
                                    EditingIntent.CreateCashback
                                )

                                CategoryTabItem.Shops -> sendIntent(
                                    EditingIntent.StartCreatingShop
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = !keyboardIsVisibleState.value) {
                        BasicFloatingActionButton(icon = Icons.Rounded.Save) {
                            sendIntent(
                                EditingIntent.SaveCategory {
                                    sendIntent(
                                        EditingIntent.NavigateToCategoryViewing(
                                            startTab = currentScreen.value.type
                                        )
                                    )
                                }
                            )
                        }
                    }
                },
                fabModifier = Modifier
                    .graphicsLayer { fabPaddingDp.floatValue = size.height.toDp().value }
                    .windowInsetsPadding(CollapsingToolbarScaffoldDefaults.contentWindowInsets),
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState) {
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
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface.animate())
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    EditableTextField(
                        text = state.category.name,
                        onTextChange = {
                            sendIntent(EditingIntent.UpdateCategoryName(it))
                            sendIntent(EditingIntent.UpdateErrorMessage(CategoryError.Name))
                        },
                        label = stringResource(R.string.category_placeholder),
                        imeAction = ImeAction.Done,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    )

                    SecondaryTabsLayout(
                        pages = tabItems,
                        pagerState = pagerState,
                        scrollEnabled = !state.isCreatingShop,
                        modifier = Modifier
                            .shadow(
                                elevation = 20.dp,
                                shape = ModalSheetDefaults.BottomSheetShape
                            )
                            .background(MaterialTheme.colorScheme.background.animate())
                            .padding(top = 8.dp)
                            .clip(ModalSheetDefaults.BottomSheetShape)
                    ) { pageIndex, page ->
                        ListContentTabPage(
                            contentState = ListState.fromCollection(
                                when (page) {
                                    CategoryTabItem.Shops -> state.shops.keys
                                    CategoryTabItem.Cashbacks -> state.cashbacks
                                }
                            ),
                            state = currentListState,
                            placeholderText = when (page) {
                                CategoryTabItem.Shops -> stringResource(R.string.empty_shops_list_editing)
                                CategoryTabItem.Cashbacks -> stringResource(R.string.empty_cashbacks_list_editing)
                            },
                            bottomSpacing = fabPaddingDp.floatValue.dp.animate()
                        ) { index, item ->
                            when (item) {
                                is Shop -> MaxCashbackOwnerComposable(
                                    cashbackOwner = item.asCashbackOwner(),
                                    maxCashbacks = state.shops[item]!!.toImmutableSet(),
                                    isEditing = true,
                                    isSwiped = state.selectedShopIndex == index,
                                    onSwipe = { isSwiped ->
                                        sendIntent(CategoryIntent.SwipeShop(index, isSwiped))
                                    },
                                    onClick = {},
                                    onClickToCashback = { cashback ->
                                        sendIntent(EditingIntent.ClickToCashback(cashback.id))
                                    },
                                    onEdit = {
                                        sendIntent(CategoryIntent.SwipeShop(null))
                                        sendIntent(EditingIntent.ClickToShop(item.id))
                                    },
                                    onDelete = {
                                        sendIntent(CategoryIntent.SwipeShop(null))
                                        sendIntent(
                                            CategoryIntent.OpenDialog(
                                                type = DialogType.ConfirmDeletion(item)
                                            )
                                        )
                                    }
                                )

                                is BasicCashback -> CashbackComposable(
                                    cashback = item,
                                    isSwiped = state.selectedCashbackIndex == index,
                                    onSwipe = { isSwiped ->
                                        sendIntent(CategoryIntent.SwipeCashback(index, isSwiped))
                                    },
                                    onClick = {
                                        sendIntent(CategoryIntent.SwipeCashback(null))
                                        sendIntent(EditingIntent.ClickToCashback(item.id))
                                    },
                                    onDelete = {
                                        sendIntent(CategoryIntent.SwipeCashback(null))
                                        sendIntent(
                                            CategoryIntent.OpenDialog(
                                                type = DialogType.ConfirmDeletion(item)
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}