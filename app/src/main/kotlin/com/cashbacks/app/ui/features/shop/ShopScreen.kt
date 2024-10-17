package com.cashbacks.app.ui.features.shop

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
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
import com.cashbacks.app.ui.composables.DropdownMenu
import com.cashbacks.app.ui.composables.DropdownMenuListContent
import com.cashbacks.app.ui.composables.EditableTextField
import com.cashbacks.app.ui.composables.EmptyList
import com.cashbacks.app.ui.composables.NewNameTextField
import com.cashbacks.app.ui.composables.OnLifecycleEvent
import com.cashbacks.app.ui.features.cashback.CashbackArgs
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ShopScreen(
    viewModel: ShopViewModel,
    navigateBack: () -> Unit,
    navigateToCashback: (args: CashbackArgs) -> Unit
) {

    val snackbarHostState = remember(::SnackbarHostState)
    val keyboardState = keyboardAsState()
    var dialogType by rememberSaveable { mutableStateOf<DialogType?>(null) }

    OnLifecycleEvent(
        onDestroy = {
            if (viewModel.state.value == ViewModelState.Editing) {
                scope.launch { viewModel.saveShop() }
            }
        }
    )

    BackHandler(
        enabled = viewModel.viewModelState == ViewModelState.Editing && viewModel.shop.haveChanges
    ) {
        viewModel.push(ShopAction.OpenDialog(DialogType.Save))
    }


    LaunchedEffect(viewModel.isCreatingCategory) {
        if (viewModel.isCreatingCategory) {
            viewModel.push(ShopAction.HideCategoriesSelection)
        }
    }

    LaunchedEffect(viewModel.showCategoriesSelection) {
        if (viewModel.showCategoriesSelection) {
            viewModel.push(ShopAction.CancelCreatingCategory)
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { keyboardState.value }
            .onEach { isKeyboardOpen ->
                if (!isKeyboardOpen) {
                    viewModel.push(ShopAction.CancelCreatingCategory)
                }
            }
            .launchIn(this)

        viewModel.eventFlow.onEach { event ->
            when (event) {
                is ShopEvent.NavigateBack -> navigateBack()
                is ShopEvent.NavigateToCashback -> navigateToCashback(event.args)
                is ShopEvent.ChangeOpenedDialog -> dialogType = event.openedDialogType
                is ShopEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }.launchIn(this)
    }

    when (val type = dialogType) {
        is DialogType.ConfirmDeletion<*> -> {
            ConfirmDeletionDialog(
                text = when (type.value) {
                    is Shop -> stringResource(R.string.confirm_shop_deletion, type.value.name)
                    is Cashback -> stringResource(R.string.confirm_cashback_deletion)
                    else -> ""
                },
                onConfirm = {
                    when (type.value) {
                        is Shop -> viewModel.push(
                            action = ShopAction.Delete {
                                viewModel.push(ShopAction.ClickButtonBack)
                            }
                        )

                        is Cashback -> viewModel.push(ShopAction.DeleteCashback(type.value))
                    }
                },
                onClose = { viewModel.push(ShopAction.CloseDialog) }
            )
        }

        DialogType.Save -> {
            ConfirmExitWithSaveDataDialog(
                onConfirm = {
                    viewModel.push(ShopAction.Save)
                    viewModel.push(ShopAction.ClickButtonBack)
                },
                onDismiss = { viewModel.push(ShopAction.ClickButtonBack) },
                onClose = { viewModel.push(ShopAction.CloseDialog) }
            )
        }

        else -> {}
    }


    Box(
        modifier = Modifier.imePadding().fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ShopScreenScaffold(
            viewModel = viewModel,
            snackbarHostState = snackbarHostState
        )

        AnimatedVisibility(
            visible = viewModel.state != ScreenState.Loading && viewModel.isCreatingCategory,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            ),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            NewNameTextField(placeholder = stringResource(R.string.category_placeholder)) { name ->
                viewModel.addCategory(name)
                viewModel.addingCategoryState = false
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ShopScreenScaffold(
    viewModel: ShopViewModel,
    snackbarHostState: SnackbarHostState
) {
    val lazyListState = rememberLazyListState()
    val keyboardIsVisibleState = keyboardAsState()
    val context = LocalContext.current

    CollapsingToolbarScaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (viewModel.viewModelState) {
                            ViewModelState.Editing -> stringResource(R.string.shop_title)
                            ViewModelState.Viewing -> viewModel.shop.name
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    Crossfade(
                        targetState = viewModel.shop.haveChanges,
                        label = "is changed anim",
                        animationSpec = tween(durationMillis = 500, easing = LinearEasing)
                    ) { isChanged ->
                        IconButton(
                            onClick = {
                                when {
                                    isChanged -> viewModel.push(ShopAction.OpenDialog(DialogType.Save))
                                    else -> viewModel.push(ShopAction.ClickButtonBack)
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
                        visible = viewModel.viewModelState == ViewModelState.Editing
                                && viewModel.shop.id != null
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.onItemClick {
                                    viewModel.push(
                                        action = ShopAction.OpenDialog(
                                            DialogType.ConfirmDeletion(viewModel.shop.mapToShop())
                                        )
                                    )
                                }
                            },
                            enabled = viewModel.state != ScreenState.Loading
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
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary.animate()
                )
            )
        },
        contentState = lazyListState,
        snackbarHost = {
            SnackbarHost(snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    shape = MaterialTheme.shapes.medium,
                    containerColor = MaterialTheme.colorScheme.onBackground.animate(),
                    contentColor = MaterialTheme.colorScheme.background.animate()
                )
            }
        },
        floatingActionButtons = {
            AnimatedVisibility(
                visible = viewModel.viewModelState == ViewModelState.Editing
                        && viewModel.shop.id != null
                        && !keyboardIsVisibleState.value,
                enter = floatingActionButtonEnterAnimation(),
                exit = floatingActionButtonExitAnimation()
            ) {
                BasicFloatingActionButton(icon = Icons.Rounded.Add) {
                    viewModel.onItemClick {
                        viewModel.push(ShopAction.CreateCashback)
                    }
                }
            }

            AnimatedVisibility(visible = !keyboardIsVisibleState.value) {
                BasicFloatingActionButton(
                    icon = when (viewModel.viewModelState) {
                        ViewModelState.Editing -> Icons.Rounded.Save
                        ViewModelState.Viewing -> Icons.Rounded.Edit
                    }
                ) {
                    viewModel.onItemClick {
                        val action = when (viewModel.viewModelState) {
                            ViewModelState.Editing -> ShopAction.Save
                            ViewModelState.Viewing -> ShopAction.Edit
                        }
                        viewModel.push(action)
                    }
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
            targetState = viewModel.state,
            label = "content loading animation",
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            modifier = Modifier.fillMaxSize()
        ) { state ->
            when (state) {
                ScreenState.Loading -> LoadingInBox()
                else -> ShopScreenContent(
                    viewModel = viewModel,
                    state = lazyListState,
                    bottomPadding = with(LocalDensity.current) { fabHeightPx.floatValue.toDp() }
                )
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ShopScreenContent(
    viewModel: ShopViewModel,
    state: LazyListState,
    bottomPadding: Dp
) {
    LazyColumn(
        state = state,
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        if (viewModel.viewModelState == ViewModelState.Editing) {
            stickyHeader {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.background(MaterialTheme.colorScheme.background.animate())
                ) {
                    ExposedDropdownMenuBox(
                        expanded = viewModel.showCategoriesSelection,
                        onExpandedChange = { isExpanded ->
                            val action = when {
                                isExpanded -> ShopAction.ShowCategoriesSelection
                                else -> ShopAction.HideCategoriesSelection
                            }
                            viewModel.push(action)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        EditableTextField(
                            text = viewModel.shop.parentCategory?.name
                                ?: stringResource(R.string.value_not_selected),
                            onTextChange = {},
                            enabled = false,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            error = viewModel.showErrors
                                    && viewModel.shop.errors[ShopError.Parent] != null,
                            errorMessage = viewModel.shop.errors[ShopError.Parent] ?: "",
                            label = stringResource(R.string.category_title),
                            leadingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = viewModel.showCategoriesSelection
                                )
                            },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )

                        DropdownMenu(
                            itemsFlow = viewModel.categoriesStateFlow,
                            expanded = viewModel.showCategoriesSelection,
                            onClose = { viewModel.push(ShopAction.HideCategoriesSelection) }
                        ) { categories ->
                            DropdownMenuListContent(
                                list = categories,
                                selected = { viewModel.shop.parentCategory?.id == it.id },
                                title = Category::name,
                                onClick = {
                                    with(viewModel) {
                                        shop.updateValue(shop::parentCategory, it)
                                        if (showErrors) {
                                            updateShopErrorMessage(ShopError.Parent)
                                        }
                                        push(ShopAction.HideCategoriesSelection)
                                    }
                                },
                                addButton = {
                                    TextButton(
                                        onClick = {
                                            viewModel.push(ShopAction.StartCreatingCategory)
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
                        text = viewModel.shop.name,
                        onTextChange = {
                            with(viewModel) {
                                shop.updateValue(shop::name, it)
                                if (showErrors) {
                                    updateShopErrorMessage(ShopError.Name)
                                }
                            }
                        },
                        label = stringResource(R.string.shop_placeholder),
                        imeAction = ImeAction.Done,
                        error = viewModel.showErrors
                                && viewModel.shop.errors[ShopError.Name] != null,
                        errorMessage = viewModel.shop.errors[ShopError.Name] ?: "",
                        enabled = viewModel.viewModelState == ViewModelState.Editing,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    HorizontalDivider(Modifier.padding(horizontal = 8.dp))
                }
            }
        }

        if (viewModel.shop.cashbacks.isEmpty()) {
            if (viewModel.shop.id != null) {
                item {
                    EmptyList(
                        text = when (viewModel.viewModelState) {
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
            itemsIndexed(viewModel.shop.cashbacks) { index, cashback ->
                CashbackComposable(
                    cashback = cashback,
                    isSwiped = viewModel.selectedCashbackIndex == index,
                    onSwipe = { isSwiped ->
                        viewModel.push(ShopAction.SwipeCashback(isSwiped, index))
                    },
                    onClick = {
                        viewModel.onItemClick {
                            viewModel.push(ShopAction.SwipeCashback(isOpened = false))
                            viewModel.push(ShopAction.NavigateToCashback(cashback.id))
                        }
                    },
                    onDelete = {
                        viewModel.onItemClick {
                            viewModel.push(ShopAction.SwipeCashback(isOpened = false))
                            viewModel.push(
                                action = ShopAction.OpenDialog(
                                    type = DialogType.ConfirmDeletion(cashback)
                                )
                            )
                        }
                    }
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