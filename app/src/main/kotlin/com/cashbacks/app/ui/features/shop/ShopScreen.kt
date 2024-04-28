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
    popBackStack: () -> Unit,
    navigateToCashback: (args: CashbackArgs) -> Unit
) {

    val snackbarHostState = remember(::SnackbarHostState)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardState = keyboardAsState()

    OnLifecycleEvent(
        onDestroy = {
            if (viewModel.state.value == ViewModelState.Editing) {
                scope.launch { viewModel.saveShop() }
            }
        }
    )

    BackHandler(enabled = viewModel.isEditing.value && viewModel.shop.value.haveChanges) {
        viewModel.openDialog(DialogType.Save)
    }

    val showSnackbar = remember {
        fun(message: String) {
            scope.launch { snackbarHostState.showSnackbar(message) }
        }
    }

    LaunchedEffect(viewModel.addingCategoryState) {
        if (viewModel.addingCategoryState) {
            viewModel.showCategoriesSelection = false
        }
    }

    LaunchedEffect(viewModel.showCategoriesSelection) {
        if (viewModel.showCategoriesSelection) {
            viewModel.addingCategoryState = false
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { keyboardState.value }.collect { isKeyboardOpen ->
            if (!isKeyboardOpen) {
                viewModel.addingCategoryState = false
            }
        }
    }

    var dialogType by rememberSaveable { mutableStateOf<DialogType?>(null) }
    LaunchedEffect(key1 = true) {
        viewModel.eventsFlow.collect { event ->
            when (event) {
                is ScreenEvents.Navigate -> (event.args as? CashbackArgs)
                    ?.let(navigateToCashback)
                    ?: popBackStack()
                is ScreenEvents.ShowSnackbar -> showSnackbar(event.message)
                is ScreenEvents.OpenDialog -> dialogType = event.type
                ScreenEvents.CloseDialog -> dialogType = null
            }
        }
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
                        is Shop -> viewModel.deleteShop {
                            viewModel.navigateTo(null)
                        }

                        is Cashback -> viewModel.deleteCashback(type.value)
                    }
                },
                onClose = viewModel::closeDialog
            )
        }

        DialogType.Save -> {
            ConfirmExitWithSaveDataDialog(
                onConfirm = {
                    viewModel.save(context.resources)
                    popBackStack()
                },
                onDismiss = popBackStack,
                onClose = viewModel::closeDialog
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
            visible = viewModel.state.value != ViewModelState.Loading && viewModel.addingCategoryState,
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
                        text = viewModel.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    Crossfade(
                        targetState = viewModel.shop.value.haveChanges,
                        label = "is changed anim",
                        animationSpec = tween(durationMillis = 500, easing = LinearEasing)
                    ) { isChanged ->
                        IconButton(
                            onClick = {
                                when {
                                    isChanged -> viewModel.openDialog(DialogType.Save)
                                    else -> viewModel.navigateTo(null)
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
                        visible = viewModel.isEditing.value && viewModel.shopId != null
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.openDialog(
                                    DialogType.ConfirmDeletion(viewModel.shop.value.mapToShop())
                                )
                            },
                            enabled = !viewModel.isLoading.value
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
                visible = viewModel.isEditing.value
                        && viewModel.shopId != null
                        && !keyboardIsVisibleState.value,
                enter = floatingActionButtonEnterAnimation(),
                exit = floatingActionButtonExitAnimation()
            ) {
                BasicFloatingActionButton(icon = Icons.Rounded.Add) {
                    when (viewModel.shopId) {
                        null -> viewModel.save(context.resources, onSuccess = viewModel::addCashback)
                        else -> viewModel.addCashback()
                    }
                }
            }

            AnimatedVisibility(visible = !keyboardIsVisibleState.value) {
                BasicFloatingActionButton(
                    icon = when {
                        viewModel.isEditing.value -> Icons.Rounded.Save
                        else -> Icons.Rounded.Edit
                    },
                    onClick = {
                        when {
                            viewModel.isEditing.value -> viewModel.save(context.resources)
                            else -> viewModel.edit()
                        }
                    }
                )
            }

        },
        contentWindowInsets = WindowInsets.ime.only(WindowInsetsSides.Bottom),
        fabModifier = Modifier
            .graphicsLayer { viewModel.fabPaddingDp.floatValue = size.height.toDp().value }
            .windowInsetsPadding(CollapsingToolbarScaffoldDefaults.contentWindowInsets),
        modifier = Modifier
            .imePadding()
            .fillMaxSize()
    ) {
        Crossfade(
            targetState = viewModel.state.value,
            label = "content loading animation",
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            modifier = Modifier.fillMaxSize()
        ) { state ->
            when (state) {
                ViewModelState.Loading -> LoadingInBox()
                else -> ShopScreenContent(viewModel, lazyListState)
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShopScreenContent(
    viewModel: ShopViewModel,
    state: LazyListState
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        if (viewModel.isEditing.value) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = viewModel.showCategoriesSelection,
                    onExpandedChange = viewModel::showCategoriesSelection::set
                ) {
                    EditableTextField(
                        text = viewModel.shop.value.category?.name
                            ?: stringResource(R.string.value_not_selected),
                        onTextChange = {},
                        enabled = false,
                        textStyle = MaterialTheme.typography.bodyMedium,
                        error = viewModel.showErrors
                                && viewModel.shop.value.categoryErrorMessage.value.isNotBlank(),
                        errorMessage = viewModel.shop.value.categoryErrorMessage.value,
                        label = stringResource(R.string.category_title),
                        leadingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = viewModel.showCategoriesSelection
                            )
                        },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    DropdownMenu(
                        listLiveData = viewModel.getAllCategories(),
                        expanded = viewModel.showCategoriesSelection,
                        onClose = { viewModel.showCategoriesSelection = false }
                    ) { categories ->
                        DropdownMenuListContent(
                            list = categories,
                            selectedItem = viewModel.shop.value.category,
                            title = Category::name,
                            onClick = {
                                with(viewModel.shop.value) {
                                    updateValue(this::category, it)
                                    if (viewModel.showErrors) updateCategoryError(context.resources)
                                }
                                viewModel.showCategoriesSelection = false
                            },
                            addButton = {
                                TextButton(
                                    onClick = { viewModel.addingCategoryState = true },
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
                    text = viewModel.shop.value.name,
                    onTextChange = {
                        with(viewModel.shop.value) {
                            updateValue(::name, it)
                            if (viewModel.showErrors) updateNameError(context.resources)
                        }
                    },
                    label = stringResource(R.string.shop_placeholder),
                    imeAction = ImeAction.Done,
                    error = viewModel.showErrors
                            && viewModel.shop.value.nameErrorMessage.value.isNotBlank(),
                    enabled = viewModel.state.value == ViewModelState.Editing
                )
                HorizontalDivider(Modifier.padding(horizontal = 8.dp))
            }
        }

        val cashbacksState = viewModel.cashbacksLiveData.observeAsState()

        Crossfade(
            targetState = cashbacksState.value,
            label = "cashbacks animation",
            animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
        ) { cashbacks ->
            when {
                cashbacks == null -> LoadingInBox()
                cashbacks.isEmpty() && viewModel.shopId != null -> EmptyList(
                    text = when {
                        viewModel.isEditing.value -> stringResource(R.string.empty_cashbacks_list_editing)
                        else -> stringResource(R.string.empty_cashbacks_list_editing)
                    },
                    icon = Icons.Rounded.DataArray,
                    iconModifier = Modifier.scale(2.5f)
                )

                else -> {
                    LazyColumn(
                        state = state,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(cashbacks) { index, cashback ->
                            CashbackComposable(
                                cashback = cashback,
                                isSwiped = viewModel.selectedCashbackIndex == index,
                                onSwipe = { isSwiped ->
                                    viewModel.selectedCashbackIndex = when {
                                        isSwiped -> index
                                        else -> null
                                    }
                                },
                                onClick = {
                                    viewModel.onItemClick {
                                        viewModel.navigateTo(
                                            args = CashbackArgs.Shop.Existing(
                                                cashbackId = cashback.id,
                                                shopId = viewModel.shopId!!
                                            )
                                        )
                                    }
                                },
                                onDelete = {
                                    viewModel.onItemClick {
                                        viewModel.selectedCashbackIndex = null
                                        viewModel.openDialog(DialogType.ConfirmDeletion(cashback))
                                    }
                                }
                            )
                        }

                        item {
                            Spacer(
                                modifier = Modifier.height(viewModel.fabPaddingDp.floatValue.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}