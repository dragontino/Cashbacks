package com.cashbacks.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.DataArray
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditOff
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.cashbacks.app.R
import com.cashbacks.app.ui.composables.BasicFloatingActionButton
import com.cashbacks.app.ui.composables.CashbackComposable
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.ConfirmDeletionDialog
import com.cashbacks.app.ui.composables.ConfirmExitWithSaveDataDialog
import com.cashbacks.app.ui.composables.DisposableEffectWithLifecycle
import com.cashbacks.app.ui.composables.EditableTextField
import com.cashbacks.app.ui.composables.EmptyList
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.ui.screens.navigation.AppScreens
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.floatingActionButtonEnterAnimation
import com.cashbacks.app.util.floatingActionButtonExitAnimation
import com.cashbacks.app.viewmodel.ShopViewModel
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Shop
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    viewModel: ShopViewModel,
    popBackStack: () -> Unit,
    navigateTo: (route: String) -> Unit
) {
    val snackbarHostState = remember(::SnackbarHostState)
    val scope = rememberCoroutineScope()
    
    DisposableEffectWithLifecycle(
        onDestroy = {
            if (viewModel.state.value == ViewModelState.Editing) {
                viewModel.save()
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
    
    if (viewModel.showDialog) {
        when (val type = viewModel.dialogType) {
            is DialogType.ConfirmDeletion<*> -> {
                ConfirmDeletionDialog(
                    text = when (type.value) {
                        is Shop -> stringResource(R.string.confirm_shop_deletion, type.value.name)
                        is Cashback -> stringResource(R.string.confrim_cashback_deletion)
                        else -> ""
                    },
                    onConfirm = {
                        when (type.value) {
                            is Shop -> {
                                viewModel.deleteShop()
                                viewModel.closeDialog() 
                                popBackStack()
                            }
                            is Cashback -> {
                                viewModel.deleteCashback(type.value, errorMessage = showSnackbar)
                            }
                        }
                    },
                    onDismiss = viewModel::closeDialog
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
            null -> {}
        }
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
                    containerColor = MaterialTheme.colorScheme.primary.animate(),
                    titleContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary.animate()
                )
            )
        },
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
                visible = viewModel.isEditing.value,
                enter = floatingActionButtonEnterAnimation(),
                exit = floatingActionButtonExitAnimation()
            ) {
                BasicFloatingActionButton(
                    icon = Icons.Rounded.Add,
                    onClick = {
                        navigateTo(AppScreens.Cashback.createUrlFromShop(id = null, shopId = viewModel.shopId))
                    }
                )
            }

            BasicFloatingActionButton(
                icon = when {
                    viewModel.isEditing.value -> Icons.Rounded.EditOff
                    else -> Icons.Rounded.Edit
                },
                onClick = {
                    when {
                        viewModel.isEditing.value -> viewModel.save()
                        else -> viewModel.edit()
                    }
                }
            )
        },
    ) { contentPadding ->
        Crossfade(
            targetState = viewModel.state.value,
            label = "content loading animation",
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            modifier = Modifier
                .imePadding()
                .padding(contentPadding)
                .fillMaxSize()
        ) { state ->
            when (state) {
                ViewModelState.Loading -> LoadingInBox()
                else -> ShopScreenContent(
                    viewModel = viewModel,
                    navigateTo = navigateTo
                )
            }
        }
    }
}



@Composable
private fun ShopScreenContent(
    viewModel: ShopViewModel,
    navigateTo: (route: String) -> Unit
) {
    val listState = rememberLazyListState()

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(vertical = 16.dp)
            .fillMaxSize()
    ) {
        if (viewModel.isEditing.value) {
            EditableTextField(
                text = viewModel.shop.value.name,
                onTextChange = { viewModel.shop.value.name = it },
                label = stringResource(R.string.shop_placeholder),
                imeAction = ImeAction.Done,
                enabled = viewModel.state.value == ViewModelState.Editing,
                modifier = Modifier.padding(horizontal = 16.dp)
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
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(cashbacks) {
                            CashbackComposable(
                                cashback = it,
                                onClick = {
                                    navigateTo(
                                        AppScreens.Cashback.createUrlFromShop(
                                            id = it.id,
                                            shopId = viewModel.shopId
                                        )
                                    )
                                },
                                onDelete = {
                                    viewModel.openDialog(DialogType.ConfirmDeletion(it))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}