package com.cashbacks.app.ui.features.shop

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.cashbacks.app.R
import com.cashbacks.app.ui.composables.BasicFloatingActionButton
import com.cashbacks.app.ui.composables.CashbackComposable
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffoldDefaults
import com.cashbacks.app.ui.composables.ConfirmDeletionDialog
import com.cashbacks.app.ui.composables.ConfirmExitWithSaveDataDialog
import com.cashbacks.app.ui.composables.OnLifecycleEvent
import com.cashbacks.app.ui.composables.EditableTextField
import com.cashbacks.app.ui.composables.EmptyList
import com.cashbacks.app.ui.features.cashback.CashbackArgs
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ScreenEvents
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.floatingActionButtonEnterAnimation
import com.cashbacks.app.util.floatingActionButtonExitAnimation
import com.cashbacks.app.util.keyboardAsState
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Shop
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ShopScreen(
    viewModel: ShopViewModel,
    popBackStack: () -> Unit,
    navigateToCashback: (args: CashbackArgs) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val snackbarHostState = remember(::SnackbarHostState)
    val scope = rememberCoroutineScope()
    val keyboardIsVisibleState = keyboardAsState()
    
    OnLifecycleEvent(
        onDestroy = {
            if (viewModel.state.value == ViewModelState.Editing) {
                scope.launch { viewModel.saveShop() }
            }
        }
    )

    BackHandler {
        when (viewModel.state.value) {
            ViewModelState.Editing -> viewModel.save()
            else -> popBackStack()
        }
    }
    
    val showSnackbar = remember {
        fun (message: String) {
            scope.launch { snackbarHostState.showSnackbar(message) }
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
                        is Shop -> {
                            viewModel.deleteShop()
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
                    viewModel.save()
                    popBackStack()
                },
                onDismiss = popBackStack,
                onClose = viewModel::closeDialog
            )
        }
        else -> {}
    }

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
                        targetState = viewModel.shop.value.isChanged,
                        label = "is changed anim"
                    ) { isChanged ->
                        IconButton(
                            onClick = {
                                when {
                                    isChanged -> viewModel.openDialog(DialogType.Save)
                                    else -> popBackStack()
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
                    AnimatedVisibility(visible = viewModel.isEditing.value) {
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
                visible = viewModel.isEditing.value && !keyboardIsVisibleState.value,
                enter = floatingActionButtonEnterAnimation(),
                exit = floatingActionButtonExitAnimation()
            ) {
                BasicFloatingActionButton(
                    icon = Icons.Rounded.Add,
                    onClick = {
                        viewModel.navigateTo(
                            args = CashbackArgs.New.Shop(
                                cashbackId = null,
                                shopId = viewModel.shopId
                            )
                        )
                    }
                )
            }

            AnimatedVisibility(visible = !keyboardIsVisibleState.value) {
                BasicFloatingActionButton(
                    icon = when {
                        viewModel.isEditing.value -> Icons.Rounded.Save
                        else -> Icons.Rounded.Edit
                    },
                    onClick = {
                        when {
                            viewModel.isEditing.value -> viewModel.save()
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



@Composable
private fun ShopScreenContent(
    viewModel: ShopViewModel,
    state: LazyListState
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (viewModel.isEditing.value) {
            EditableTextField(
                text = viewModel.shop.value.name,
                onTextChange = { viewModel.shop.value.name = it },
                label = stringResource(R.string.shop_placeholder),
                imeAction = ImeAction.Done,
                enabled = viewModel.state.value == ViewModelState.Editing,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            HorizontalDivider(Modifier.padding(horizontal = 8.dp))
        }


        val cashbacksState = viewModel.cashbacksLiveData.observeAsState()

        Crossfade(
            targetState = cashbacksState.value,
            label = "cashbacks animation",
            animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
        ) { cashbacks ->
            when {
                cashbacks == null -> LoadingInBox()
                cashbacks.isEmpty() -> EmptyList(
                    text = stringResource(R.string.empty_cashbacks_list),
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
                                        viewModel.navigateTo(CashbackArgs.Existing(cashback.id))
                                    }
                                },
                                onDelete = {
                                    viewModel.openDialog(DialogType.ConfirmDeletion(cashback))
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