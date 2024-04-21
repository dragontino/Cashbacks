package com.cashbacks.app.ui.features.category.editing

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.cashbacks.app.ui.composables.BasicFloatingActionButton
import com.cashbacks.app.ui.composables.CashbackComposable
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffoldDefaults
import com.cashbacks.app.ui.composables.ConfirmDeletionDialog
import com.cashbacks.app.ui.composables.ConfirmExitWithSaveDataDialog
import com.cashbacks.app.ui.composables.EditableTextField
import com.cashbacks.app.ui.composables.ListContentTabPage
import com.cashbacks.app.ui.composables.ModalSheetDefaults
import com.cashbacks.app.ui.composables.NewNameTextField
import com.cashbacks.app.ui.composables.OnLifecycleEvent
import com.cashbacks.app.ui.composables.SecondaryTabsLayout
import com.cashbacks.app.ui.composables.ShopComposable
import com.cashbacks.app.ui.features.cashback.CashbackArgs
import com.cashbacks.app.ui.features.category.CategoryArgs
import com.cashbacks.app.ui.features.category.TabItem
import com.cashbacks.app.ui.features.shop.ShopArgs
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ScreenEvents
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.floatingActionButtonEnterAnimation
import com.cashbacks.app.util.floatingActionButtonExitAnimation
import com.cashbacks.app.util.keyboardAsState
import com.cashbacks.domain.R
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.Shop
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CategoryEditingScreen(
    viewModel: CategoryEditingViewModel,
    startTab: TabItem,
    navigateToCategory: (args: CategoryArgs) -> Unit,
    navigateToShop: (args: ShopArgs) -> Unit,
    navigateToCashback: (args: CashbackArgs) -> Unit,
    popBackStack: () -> Unit
) {
    BackHandler {
        when {
            viewModel.category.value.isChanged -> viewModel.openDialog(DialogType.Save)
            else -> viewModel.navigateTo(null)
        }
    }

    OnLifecycleEvent(onDestroy = viewModel::save)

    val snackbarState = remember(::SnackbarHostState)
    val scope = rememberCoroutineScope()

    val showSnackbar = remember {
        fun(message: String) {
            scope.launch { snackbarState.showSnackbar(message) }
        }
    }


    val keyboardIsOpen = keyboardAsState()
    LaunchedEffect(Unit) {
        snapshotFlow { keyboardIsOpen.value }.collectLatest { isKeyboardOpen ->
            if (!isKeyboardOpen) {
                viewModel.addingShopState.value = false
            }
        }
    }

    var dialogType: DialogType? by rememberSaveable { mutableStateOf(null) }
    LaunchedEffect(key1 = true) {
        viewModel.eventsFlow.collect { event ->
            when (event) {
                is ScreenEvents.Navigate -> {
                    when (event.args) {
                        is CategoryArgs -> event.args.let(navigateToCategory)
                        is ShopArgs -> event.args.let(navigateToShop)
                        is CashbackArgs -> event.args.let(navigateToCashback)
                        null -> popBackStack()
                    }
                }
                is ScreenEvents.ShowSnackbar -> showSnackbar(event.message)
                is ScreenEvents.OpenDialog -> dialogType = event.type
                ScreenEvents.CloseDialog -> dialogType = null
            }
        }
    }


    when (dialogType) {
        is DialogType.ConfirmDeletion<*> -> {
            val value = (dialogType as DialogType.ConfirmDeletion<*>).value
            ConfirmDeletionDialog(
                text = when (value) {
                    is Category -> stringResource(
                        R.string.confirm_category_deletion,
                        viewModel.category.value.name
                    )
                    is Shop -> stringResource(R.string.confirm_shop_deletion, value.name)
                    is Cashback -> stringResource(R.string.confirm_cashback_deletion)
                    else -> ""
                },
                onConfirm = {
                    when (value) {
                        is Category -> viewModel.deleteCategory()
                        is Shop -> viewModel.deleteShop(value)
                        is Cashback -> viewModel.deleteCashback(value)
                    }
                },
                onClose = viewModel::closeDialog
            )
        }
        DialogType.Save -> {
            ConfirmExitWithSaveDataDialog(
                onConfirm = {
                    viewModel.onItemClick {
                        viewModel.save { viewModel.navigateTo(null) }
                    }
                },
                onDismiss = {
                    viewModel.onItemClick {
                        viewModel.navigateTo(null)
                    }
                },
                onClose = viewModel::closeDialog
            )
        }
        else -> {}
    }

    Box(
        modifier = Modifier
            .imePadding()
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (viewModel.state.value) {
            ViewModelState.Loading -> LoadingInBox(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            )
            else -> CategoryInfoScreenContent(
                viewModel = viewModel,
                startTab = startTab,
                snackbarState = snackbarState,
            )
        }

        AnimatedVisibility(
            visible = viewModel.state.value != ViewModelState.Loading && viewModel.addingShopState.value,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            ),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            NewNameTextField(
                placeholder = stringResource(R.string.shop_placeholder),
            ) { name ->
                viewModel.addShop(name)
                viewModel.addingShopState.value = false
            }
        }
    }
}



@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class,
)
@Composable
private fun CategoryInfoScreenContent(
    viewModel: CategoryEditingViewModel,
    startTab: TabItem,
    snackbarState: SnackbarHostState
) {
    val fabPaddingDp = rememberSaveable { mutableFloatStateOf(0f) }
    val keyboardIsVisibleState = keyboardAsState()

    val tabItems = TabItem.entries
    val pagerState = rememberPagerState(initialPage = tabItems.indexOf(startTab)) { tabItems.size }
    val listStates = Array(2) { rememberLazyListState() }

    val currentScreen = remember(pagerState.currentPage) {
        derivedStateOf { tabItems[pagerState.currentPage] }
    }
    val currentListState = remember(pagerState.currentPage) {
        listStates[pagerState.currentPage]
    }

    CollapsingToolbarScaffold(
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
                        targetState = viewModel.category.value.isChanged,
                        label = "icon animation",
                        animationSpec = tween(durationMillis = 200, easing = LinearEasing)
                    ) { isChanged ->
                        IconButton(
                            onClick = {
                                viewModel.onItemClick {
                                    when {
                                        isChanged -> viewModel.openDialog(DialogType.Save)
                                        else -> viewModel.navigateTo(null)
                                    }
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
                            viewModel.onItemClick {
                                viewModel.openDialog(DialogType.ConfirmDeletion(viewModel.category))
                            }
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
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary.animate()
                )
            )
        },
        contentState = currentListState,
        floatingActionButtons = {
            AnimatedVisibility(
                visible = !keyboardIsVisibleState.value,
                enter = floatingActionButtonEnterAnimation(),
                exit = floatingActionButtonExitAnimation()
            ) {
                BasicFloatingActionButton(icon = Icons.Rounded.Add) {
                    viewModel.onItemClick {
                        when (currentScreen.value) {
                            TabItem.Cashbacks -> viewModel.navigateTo(
                                args = CashbackArgs.Category.New(viewModel.categoryId)
                            )

                            TabItem.Shops -> viewModel.addingShopState.value = true
                        }
                    }
                }
            }

            AnimatedVisibility(visible = !keyboardIsVisibleState.value) {
                BasicFloatingActionButton(icon = Icons.Rounded.Save) {
                    viewModel.onItemClick {
                        viewModel.save {
                            viewModel.navigateTo(
                                args = CategoryArgs(
                                    id = viewModel.categoryId,
                                    isEditing = false,
                                    startTab = currentScreen.value
                                )
                            )
                        }
                    }
                }
            }
        },
        fabModifier = Modifier
            .graphicsLayer { fabPaddingDp.floatValue = size.height.toDp().value }
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
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface.animate())
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EditableTextField(
                text = viewModel.category.value.name,
                onTextChange = viewModel.category.value::name::set,
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
                scrollEnabled = !viewModel.addingShopState.value,
                modifier = Modifier
                    .shadow(elevation = 20.dp, shape = ModalSheetDefaults.BottomSheetShape)
                    .background(MaterialTheme.colorScheme.background.animate())
                    .padding(top = 8.dp)
                    .clip(ModalSheetDefaults.BottomSheetShape)
            ) { pageIndex, page ->
                ListContentTabPage(
                    items = when (page) {
                        TabItem.Shops -> viewModel.shopsLiveData.observeAsState().value
                        TabItem.Cashbacks -> viewModel.cashbacksLiveData.observeAsState().value
                    },
                    state = listStates[pageIndex],
                    placeholderText = when (page) {
                        TabItem.Shops -> stringResource(R.string.empty_shops_list_editing)
                        TabItem.Cashbacks -> stringResource(R.string.empty_cashbacks_list_editing)
                    },
                    bottomSpacing = fabPaddingDp.floatValue.dp.animate()
                ) { index, item ->
                    when (item) {
                        is Shop -> ShopComposable(
                            shop = item,
                            isEditing = true,
                            isSwiped = viewModel.selectedShopIndex == index,
                            onSwipe = { isSwiped ->
                                viewModel.selectedShopIndex = when {
                                    isSwiped -> index
                                    else -> null
                                }
                            },
                            onClick = {},
                            onEdit = {
                                viewModel.onItemClick {
                                    viewModel.selectedShopIndex = -1
                                    viewModel.navigateTo(
                                        args = ShopArgs.Existing(id = item.id, isEditing = true)
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
                                    viewModel.navigateTo(
                                        args = CashbackArgs.Category.Existing(
                                            cashbackId = item.id,
                                            categoryId = viewModel.categoryId
                                        )
                                    )
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
}