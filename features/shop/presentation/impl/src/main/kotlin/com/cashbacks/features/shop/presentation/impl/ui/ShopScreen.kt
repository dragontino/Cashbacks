package com.cashbacks.features.shop.presentation.impl.ui

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.DataArray
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cashbacks.common.composables.BasicFloatingActionButton
import com.cashbacks.common.composables.BoundedSnackbar
import com.cashbacks.common.composables.CollapsingToolbarScaffold
import com.cashbacks.common.composables.CollapsingToolbarScaffoldDefaults
import com.cashbacks.common.composables.ConfirmDeletionDialog
import com.cashbacks.common.composables.ConfirmExitWithSaveDataDialog
import com.cashbacks.common.composables.DropdownMenuListContent
import com.cashbacks.common.composables.EditableTextField
import com.cashbacks.common.composables.EmptyList
import com.cashbacks.common.composables.ListDropdownMenu
import com.cashbacks.common.composables.LoadingInBox
import com.cashbacks.common.composables.NewNameTextField
import com.cashbacks.common.composables.management.DialogType
import com.cashbacks.common.composables.management.ScreenState
import com.cashbacks.common.composables.management.ViewModelState
import com.cashbacks.common.composables.management.toListState
import com.cashbacks.common.composables.utils.animate
import com.cashbacks.common.composables.utils.floatingActionButtonEnterAnimation
import com.cashbacks.common.composables.utils.floatingActionButtonExitAnimation
import com.cashbacks.common.composables.utils.keyboardAsState
import com.cashbacks.common.composables.utils.mix
import com.cashbacks.common.resources.R
import com.cashbacks.common.utils.mvi.IntentSender
import com.cashbacks.features.cashback.domain.model.Cashback
import com.cashbacks.features.cashback.presentation.api.CashbackArgs
import com.cashbacks.features.cashback.presentation.api.composables.CashbackComposable
import com.cashbacks.features.shop.domain.model.Shop
import com.cashbacks.features.shop.presentation.impl.mvi.ShopError
import com.cashbacks.features.shop.presentation.impl.mvi.ShopIntent
import com.cashbacks.features.shop.presentation.impl.mvi.ShopLabel
import com.cashbacks.features.shop.presentation.impl.mvi.ShopState
import com.cashbacks.features.shop.presentation.impl.viewmodel.ShopViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun ShopRoot(
    navigateBack: () -> Unit,
    navigateToCashback: (CashbackArgs) -> Unit,
    viewModel: ShopViewModel = koinViewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember(::SnackbarHostState)
    var dialogType by rememberSaveable { mutableStateOf<DialogType?>(null) }

    LaunchedEffect(Unit) {
        viewModel.labelsFlow.collect { label ->
            when (label) {
                is ShopLabel.ChangeOpenedDialog -> dialogType = label.openedDialogType
                is ShopLabel.DisplayMessage -> launch {
                    snackbarHostState.showSnackbar(label.message)
                }
                is ShopLabel.NavigateBack -> navigateBack()
                is ShopLabel.NavigateToCashback -> navigateToCashback(label.args)
            }
        }
    }


    when (val type = dialogType) {
        is DialogType.ConfirmDeletion<*> -> {
            ConfirmDeletionDialog(
                text = when (val value = type.value) {
                    is Shop -> stringResource(R.string.confirm_shop_deletion, value.name)
                    is Cashback -> stringResource(R.string.confirm_cashback_deletion)
                    else -> ""
                },
                onConfirm = {
                    when (val value = type.value) {
                        is Shop -> viewModel.sendIntent(
                            ShopIntent.Delete {
                                viewModel.sendIntent(ShopIntent.ClickButtonBack)
                            }
                        )

                        is Cashback -> viewModel.sendIntent(ShopIntent.DeleteCashback(value))
                    }
                },
                onClose = { viewModel.sendIntent(ShopIntent.CloseDialog) }
            )
        }

        DialogType.Save -> {
            ConfirmExitWithSaveDataDialog(
                onConfirm = {
                    viewModel.sendIntent(
                        ShopIntent.Save {
                            viewModel.sendIntent(ShopIntent.ClickButtonBack)
                        }
                    )
                },
                onDismiss = { viewModel.sendIntent(ShopIntent.ClickButtonBack) },
                onClose = { viewModel.sendIntent(ShopIntent.CloseDialog) }
            )
        }
    }


    ShopScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        intentSender = IntentSender(viewModel::sendIntent)
    )
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShopScreen(
    state: ShopState,
    snackbarHostState: SnackbarHostState,
    intentSender: IntentSender<ShopIntent>
) {
    val keyboardState = keyboardAsState()


    BackHandler(
        enabled = state.viewModelState == ViewModelState.Editing && state.isShopChanged()
    ) {
        intentSender.sendWithDelay(ShopIntent.OpenDialog(DialogType.Save))
    }


    LaunchedEffect(state.isCreatingCategory) {
        if (state.isCreatingCategory) {
            intentSender.send(ShopIntent.HideCategoriesSelection)
        }
    }

    LaunchedEffect(state.showCategoriesSelection) {
        if (state.showCategoriesSelection) {
            intentSender.send(ShopIntent.CancelCreatingCategory)
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { keyboardState.value }.collect { isKeyboardOpen ->
            if (!isKeyboardOpen) {
                intentSender.send(ShopIntent.CancelCreatingCategory)
            }
        }
    }




    Box(
        modifier = Modifier
            .imePadding()
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ShopScreenScaffold(
            state = state,
            snackbarHostState = snackbarHostState,
            intentSender = intentSender
        )


        AnimatedVisibility(
            visible = state.screenState == ScreenState.Stable && state.isCreatingCategory,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            ),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            NewNameTextField(placeholder = stringResource(R.string.category_placeholder)) { name ->
                intentSender.send(ShopIntent.AddCategory(name))
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ShopScreenScaffold(
    state: ShopState,
    snackbarHostState: SnackbarHostState,
    intentSender: IntentSender<ShopIntent>
) {
    val topBarState = rememberTopAppBarState()
    val lazyListState = rememberLazyListState()
    val keyboardIsVisibleState = keyboardAsState()
    val fabHeightPx = rememberSaveable { mutableFloatStateOf(0f) }

    CollapsingToolbarScaffold(
        topBarState = topBarState,
        contentState = lazyListState,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (state.viewModelState) {
                            ViewModelState.Editing -> stringResource(R.string.shop_title)
                            ViewModelState.Viewing -> state.shop.name
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.MiddleEllipsis
                    )
                },
                navigationIcon = {
                    Crossfade(
                        targetState = state.isShopChanged(),
                        label = "shop nav icon anim",
                        animationSpec = tween(durationMillis = 500, easing = LinearEasing)
                    ) { isChanged ->
                        IconButton(
                            onClick = {
                                when {
                                    isChanged -> intentSender.sendWithDelay(
                                        ShopIntent.OpenDialog(DialogType.Save)
                                    )

                                    else -> intentSender.sendWithDelay(ShopIntent.ClickButtonBack)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = when {
                                    isChanged -> Icons.Rounded.Clear
                                    else -> Icons.Rounded.ArrowBackIosNew
                                },
                                contentDescription = "return to previous screen",
                                modifier = Modifier.scale(1.2f)
                            )
                        }
                    }
                },
                actions = {
                    AnimatedVisibility(
                        visible = state.viewModelState == ViewModelState.Editing
                                && state.shop.id != null
                    ) {
                        IconButton(
                            onClick = {
                                intentSender.sendWithDelay(
                                    ShopIntent.OpenDialog(
                                        DialogType.ConfirmDeletion(state.shop.mapToShop())
                                    )
                                )
                            },
                            enabled = state.screenState == ScreenState.Stable
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "delete shop",
                                modifier = Modifier.scale(1.2f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .6f)
                        .mix(MaterialTheme.colorScheme.primary)
                        .ratio(topBarState.overlappedFraction),
                    titleContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary.animate()
                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) {
                BoundedSnackbar(
                    snackbarData = it,
                    shape = MaterialTheme.shapes.medium,
                    containerColor = MaterialTheme.colorScheme.onBackground.animate(),
                    contentColor = MaterialTheme.colorScheme.background.animate()
                )
            }
        },
        floatingActionButtons = {
            AnimatedVisibility(
                visible = state.viewModelState == ViewModelState.Editing
                        && state.shop.id != null
                        && !keyboardIsVisibleState.value,
                enter = floatingActionButtonEnterAnimation(),
                exit = floatingActionButtonExitAnimation()
            ) {
                BasicFloatingActionButton(icon = Icons.Rounded.Add) {
                    intentSender.sendWithDelay(ShopIntent.CreateCashback)
                }
            }

            AnimatedVisibility(visible = !keyboardIsVisibleState.value) {
                BasicFloatingActionButton(
                    icon = when (state.viewModelState) {
                        ViewModelState.Editing -> Icons.Rounded.Save
                        ViewModelState.Viewing -> Icons.Rounded.Edit
                    }
                ) {
                    val action = when (state.viewModelState) {
                        ViewModelState.Editing -> ShopIntent.Save()
                        ViewModelState.Viewing -> ShopIntent.ClickEditButton
                    }
                    intentSender.sendWithDelay(action)
                }
            }

        },
        contentWindowInsets = WindowInsets.ime.only(WindowInsetsSides.Bottom),
        fabModifier = Modifier
            .onGloballyPositioned { fabHeightPx.floatValue = it.size.height.toFloat() }
            .windowInsetsPadding(CollapsingToolbarScaffoldDefaults.contentWindowInsets),
        modifier = Modifier
            .imePadding()
            .fillMaxSize()
    ) {
        Crossfade(
            targetState = state.screenState,
            label = "content loading animation",
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            modifier = Modifier.fillMaxSize()
        ) { screenState ->
            when (screenState) {
                ScreenState.Loading -> LoadingInBox()
                else -> ShopScreenContent(
                    state = state,
                    intentSender = intentSender,
                    listState = lazyListState,
                    bottomPadding = with(LocalDensity.current) { fabHeightPx.floatValue.toDp() }
                )
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ShopScreenContent(
    state: ShopState,
    intentSender: IntentSender<ShopIntent>,
    listState: LazyListState,
    bottomPadding: Dp
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        if (state.viewModelState == ViewModelState.Editing) {
            stickyHeader {
                Column(
                    modifier = Modifier.background(MaterialTheme.colorScheme.background)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = state.showCategoriesSelection,
                        onExpandedChange = { isExpanded ->
                            val action = when {
                                isExpanded -> ShopIntent.ShowCategoriesSelection
                                else -> ShopIntent.HideCategoriesSelection
                            }
                            intentSender.sendWithDelay(action)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        EditableTextField(
                            text = state.shop.parentCategory?.name
                                ?: stringResource(R.string.value_not_selected),
                            onTextChange = {},
                            enabled = false,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            error = state.showErrors && ShopError.Parent in state.errors,
                            errorMessage = state.errors[ShopError.Parent],
                            label = stringResource(R.string.category_title),
                            leadingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = state.showCategoriesSelection
                                )
                            },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .padding(bottom = 16.dp)
                                .fillMaxWidth()
                        )

                        ListDropdownMenu(
                            state = state.selectionCategories.toListState(),
                            expanded = state.showCategoriesSelection,
                            onClose = { intentSender.send(ShopIntent.HideCategoriesSelection) }
                        ) { categories ->
                            DropdownMenuListContent(
                                list = categories,
                                selected = { state.shop.parentCategory?.id == it.id },
                                title = { it.name },
                                onClick = {
                                    intentSender.sendWithDelay(
                                        ShopIntent.UpdateShopParent(it),
                                        ShopIntent.UpdateErrorMessage(ShopError.Parent),
                                        ShopIntent.HideCategoriesSelection
                                    )
                                },
                                addButton = {
                                    TextButton(
                                        onClick = {
                                            intentSender.sendWithDelay(
                                                ShopIntent.StartCreatingCategory
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = stringResource(R.string.add_category),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            )
                        }
                    }


                    EditableTextField(
                        text = state.shop.name,
                        onTextChange = {
                            intentSender.send(ShopIntent.UpdateShopName(it))
                            intentSender.sendWithDelay(
                                ShopIntent.UpdateErrorMessage(ShopError.Name)
                            )
                        },
                        label = stringResource(R.string.shop_placeholder),
                        imeAction = ImeAction.Done,
                        error = state.showErrors && ShopError.Name in state.errors,
                        errorMessage = state.errors[ShopError.Name],
                        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    HorizontalDivider()
                }
            }
        }

        if (state.cashbacks.isEmpty()) {
            if (state.shop.id != null) {
                item {
                    EmptyList(
                        text = when (state.viewModelState) {
                            ViewModelState.Editing -> stringResource(R.string.empty_cashbacks_list_editing)
                            ViewModelState.Viewing -> stringResource(R.string.empty_cashbacks_list_viewing)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp),
                        icon = Icons.Rounded.DataArray,
                        iconModifier = Modifier.scale(2.5f)
                    )
                }
            }
        }

        else {
            itemsIndexed(state.cashbacks) { index, cashback ->
                CashbackComposable(
                    cashback = cashback,
                    isEnabledToSwipe = state.selectedCashbackIndex == index || state.selectedCashbackIndex == null,
                    onSwipeStatusChanged = { isOnSwipe ->
                        intentSender.sendWithDelay(ShopIntent.SwipeCashback(index, isOnSwipe))
                    },
                    onClick = {
                        intentSender.sendWithDelay(ShopIntent.NavigateToCashback(cashback.id))
                    },
                    onDelete = {
                        intentSender.sendWithDelay(
                            ShopIntent.OpenDialog(DialogType.ConfirmDeletion(cashback))
                        )
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }


        item {
            Spacer(
                modifier = Modifier.height(bottomPadding)
            )
        }
    }
}