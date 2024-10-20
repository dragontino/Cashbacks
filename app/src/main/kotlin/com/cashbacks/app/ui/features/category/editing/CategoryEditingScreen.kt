package com.cashbacks.app.ui.features.category.editing

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
import com.cashbacks.app.ui.composables.BasicFloatingActionButton
import com.cashbacks.app.ui.composables.CashbackComposable
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffoldDefaults
import com.cashbacks.app.ui.composables.ConfirmDeletionDialog
import com.cashbacks.app.ui.composables.ConfirmExitWithSaveDataDialog
import com.cashbacks.app.ui.composables.EditableTextField
import com.cashbacks.app.ui.composables.ListContentTabPage
import com.cashbacks.app.ui.composables.MaxCashbackOwnerComposable
import com.cashbacks.app.ui.composables.ModalSheetDefaults
import com.cashbacks.app.ui.composables.NewNameTextField
import com.cashbacks.app.ui.composables.OnLifecycleEvent
import com.cashbacks.app.ui.composables.SecondaryTabsLayout
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
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.floatingActionButtonEnterAnimation
import com.cashbacks.app.util.floatingActionButtonExitAnimation
import com.cashbacks.app.util.keyboardAsState
import com.cashbacks.domain.R
import com.cashbacks.domain.model.BasicCashback
import com.cashbacks.domain.model.BasicShop
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Category
import com.cashbacks.domain.model.Shop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CategoryEditingScreen(
    viewModel: CategoryEditingViewModel,
    startTab: CategoryTabItemType,
    navigateToCategory: (args: CategoryArgs) -> Unit,
    navigateToShop: (args: ShopArgs) -> Unit,
    navigateToCashback: (args: CashbackArgs) -> Unit,
    navigateBack: () -> Unit
) {
    BackHandler {
        when {
            viewModel.category.haveChanges -> viewModel.push(CategoryAction.OpenDialog(DialogType.Save))
            else -> viewModel.push(CategoryAction.ClickButtonBack)
        }
    }

    OnLifecycleEvent(
        onDestroy = { viewModel.push(CategoryAction.SaveCategory()) }
    )

    val snackbarState = remember(::SnackbarHostState)
    val keyboardIsOpen = keyboardAsState()
    val openedDialogType = rememberSaveable { mutableStateOf<DialogType?>(null) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.onEach { event ->
            when (event) {
                is CategoryEvent.OpenDialog -> openedDialogType.value = event.type
                is CategoryEvent.CloseDialog -> openedDialogType.value = null
                is CategoryEvent.ShowSnackbar -> snackbarState.showSnackbar(event.message)
                is CategoryEvent.NavigateBack -> navigateBack()
                is CategoryEvent.NavigateToCategoryViewingScreen -> navigateToCategory(event.args)
                is CategoryEvent.NavigateToShopScreen -> navigateToShop(event.args)
                is CategoryEvent.NavigateToCashbackScreen -> navigateToCashback(event.args)
                else -> {}
            }
        }.launchIn(this)


        snapshotFlow { keyboardIsOpen.value }.onEach { isKeyboardOpen ->
            if (!isKeyboardOpen) {
                viewModel.push(CategoryAction.FinishCreateShop)
            }
        }.launchIn(this)
    }


    when (val type = openedDialogType.value) {
        is DialogType.ConfirmDeletion<*> -> {
            val value = type.value
            ConfirmDeletionDialog(
                text = when (value) {
                    is Category -> stringResource(
                        R.string.confirm_category_deletion,
                        viewModel.category.name
                    )
                    is Shop -> stringResource(R.string.confirm_shop_deletion, value.name)
                    is Cashback -> stringResource(R.string.confirm_cashback_deletion)
                    else -> ""
                },
                onConfirm = {
                    when (value) {
                        is Category -> viewModel.push(CategoryAction.DeleteCategory())
                        is BasicShop -> viewModel.push(CategoryAction.DeleteShop(value))
                        is BasicCashback -> viewModel.push(CategoryAction.DeleteCashback(value))
                    }
                },
                onClose = { viewModel.push(CategoryAction.CloseDialog) }
            )
        }
        DialogType.Save -> {
            ConfirmExitWithSaveDataDialog(
                onConfirm = {
                    viewModel.onItemClick {
                        viewModel.push(
                            CategoryAction.SaveCategory {
                                viewModel.push(CategoryAction.ClickButtonBack)
                            }
                        )
                    }
                },
                onDismiss = {
                    viewModel.onItemClick {
                        viewModel.push(CategoryAction.ClickButtonBack)
                    }
                },
                onClose = { viewModel.push(CategoryAction.CloseDialog) }
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
        CategoryEditingScreenContent(
            viewModel = viewModel,
            startTab = startTab,
            snackbarState = snackbarState
        )

        AnimatedVisibility(
            visible = viewModel.state != ScreenState.Loading && viewModel.isCreatingShop.value,
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
                viewModel.push(CategoryAction.SaveShop(name))
            }
        }
    }
}



@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
)
@Composable
private fun CategoryEditingScreenContent(
    viewModel: CategoryEditingViewModel,
    startTab: CategoryTabItemType,
    snackbarState: SnackbarHostState
) {
    val fabPaddingDp = rememberSaveable { mutableFloatStateOf(0f) }
    val keyboardIsVisibleState = keyboardAsState()

    val tabItems = listOf(CategoryTabItem.Cashbacks, CategoryTabItem.Shops)
    val pagerState = rememberPagerState(
        initialPage = tabItems.indexOfFirst { it.type == startTab },
        pageCount = { tabItems.size },
    )
    val listStates = List(tabItems.size) { rememberLazyListState() }

    val currentScreen = remember(pagerState.currentPage) {
        derivedStateOf { tabItems[pagerState.currentPage] }
    }
    val currentListState = remember(pagerState.currentPage) {
        listStates[pagerState.currentPage]
    }


    Crossfade(
        targetState = viewModel.state,
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
                                targetState = viewModel.category.haveChanges,
                                label = "icon animation",
                                animationSpec = tween(
                                    durationMillis = 200,
                                    easing = LinearEasing
                                )
                            ) { isChanged ->
                                IconButton(
                                    onClick = {
                                        viewModel.onItemClick {
                                            when {
                                                isChanged -> viewModel.push(
                                                    CategoryAction.OpenDialog(DialogType.Save)
                                                )

                                                else -> viewModel.push(CategoryAction.ClickButtonBack)
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
                                        val dialogType = DialogType.ConfirmDeletion(
                                            viewModel.category.mapToCategory()
                                        )
                                        viewModel.push(CategoryAction.OpenDialog(dialogType))
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
                            viewModel.onItemClick {
                                when (currentScreen.value) {
                                    CategoryTabItem.Cashbacks -> viewModel.push(
                                        action = CategoryAction.NavigateToCashback(null)
                                    )

                                    CategoryTabItem.Shops -> viewModel.push(CategoryAction.StartCreateShop)
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = !keyboardIsVisibleState.value) {
                        BasicFloatingActionButton(icon = Icons.Rounded.Save) {
                            viewModel.onItemClick {
                                viewModel.push(
                                    CategoryAction.SaveCategory {
                                        viewModel.push(
                                            CategoryAction.NavigateToCategoryViewing(
                                                startTab = currentScreen.value.type
                                            )
                                        )
                                    }
                                )
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
                        text = viewModel.category.name,
                        onTextChange = {
                            viewModel.category.updateValue(
                                property = viewModel.category::name,
                                newValue = it
                            )
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
                        scrollEnabled = !viewModel.isCreatingShop.value,
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
                            contentState = ListState.fromList(
                                when (page) {
                                    CategoryTabItem.Shops -> viewModel.category.shops
                                    CategoryTabItem.Cashbacks -> viewModel.category.cashbacks
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
                                is BasicShop -> MaxCashbackOwnerComposable(
                                    cashbackOwner = item,
                                    isEditing = true,
                                    isSwiped = viewModel.selectedShopIndex == index,
                                    onSwipe = { isSwiped ->
                                        viewModel.push(
                                            CategoryAction.SwipeShop(
                                                index,
                                                isSwiped
                                            )
                                        )
                                    },
                                    onClick = {},
                                    onEdit = {
                                        viewModel.onItemClick {
                                            viewModel.push(
                                                CategoryAction.SwipeShop(index, false)
                                            )
                                            viewModel.push(
                                                CategoryAction.NavigateToShop(
                                                    ShopArgs(id = item.id, isEditing = true)
                                                )
                                            )
                                        }
                                    },
                                    onDelete = {
                                        viewModel.onItemClick {
                                            viewModel.push(
                                                CategoryAction.SwipeShop(
                                                    index,
                                                    false
                                                )
                                            )
                                            viewModel.push(
                                                CategoryAction.OpenDialog(
                                                    DialogType.ConfirmDeletion(
                                                        item
                                                    )
                                                )
                                            )
                                        }
                                    }
                                )

                                is BasicCashback -> CashbackComposable(
                                    cashback = item,
                                    isSwiped = viewModel.selectedCashbackIndex == index,
                                    onSwipe = { isSwiped ->
                                        viewModel.push(
                                            CategoryAction.SwipeCashback(
                                                index,
                                                isSwiped
                                            )
                                        )
                                    },
                                    onClick = {
                                        viewModel.onItemClick {
                                            viewModel.push(
                                                CategoryAction.SwipeCashback(index, false)
                                            )
                                            viewModel.push(CategoryAction.NavigateToCashback(item.id))
                                        }
                                    },
                                    onDelete = {
                                        viewModel.onItemClick {
                                            viewModel.push(
                                                CategoryAction.SwipeCashback(
                                                    index,
                                                    false
                                                )
                                            )
                                            viewModel.push(
                                                CategoryAction.OpenDialog(
                                                    DialogType.ConfirmDeletion(
                                                        item
                                                    )
                                                )
                                            )
                                        }
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