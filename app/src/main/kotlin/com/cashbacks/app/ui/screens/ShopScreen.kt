package com.cashbacks.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.cashbacks.app.R
import com.cashbacks.app.ui.composables.CashbackComposable
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.DisposableEffectWithLifecycle
import com.cashbacks.app.ui.composables.EditableTextField
import com.cashbacks.app.ui.composables.InfoScreenTopAppBar
import com.cashbacks.app.ui.screens.navigation.AppScreens
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.viewmodel.ShopViewModel
import com.cashbacks.app.viewmodel.ShopViewModel.ViewModelState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    viewModel: ShopViewModel,
    popBackStack: () -> Unit,
    navigateTo: (route: String) -> Unit
) {
    val listState = rememberLazyListState()
    val listItemsCount = remember {
        derivedStateOf { listState.layoutInfo.totalItemsCount }
    }

    DisposableEffectWithLifecycle(
        onDestroy = viewModel::saveShop
    )

    BackHandler {
        if (viewModel.isEditing.value) {
            viewModel.saveShop()
            viewModel.isEditing.value = false
        } else {
            popBackStack()
        }
    }

    CollapsingToolbarScaffold(
        topBar = {
            InfoScreenTopAppBar(
                title = stringResource(AppScreens.Shop.titleRes),
                isInEdit = viewModel.isEditing,
                isLoading = remember {
                    derivedStateOf { viewModel.state.value == ViewModelState.Loading }
                },
                onEdit = { viewModel.isEditing.value = true },
                onSave = {
                    viewModel.saveShop()
                    viewModel.isEditing.value = false
                },
                onDelete = viewModel::deleteShop,
                onBack = popBackStack
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = viewModel.showFab.value,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                ),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                )
            ) {
                ExtendedFloatingActionButton(
                    text = {
                        Text(
                            text = stringResource(
                                R.string.add_item,
                                stringResource(AppScreens.Cashback.titleRes)
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "add"
                        )
                    },
                    onClick = {
                        navigateTo(AppScreens.Cashback.createUrl(id = null))
                    },
                    expanded = !listState.canScrollForward && listItemsCount.value > 0
                )
            }
        },
        fabPosition = FabPosition.EndOverlay
    ) { contentPadding ->

        Crossfade(
            targetState = viewModel.state.value,
            label = "content loading animation",
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) { state ->
            when (state) {
                ViewModelState.Loading -> LoadingInBox()
                ViewModelState.Ready -> ShopScreenContent(
                    listState = listState,
                    viewModel = viewModel
                )
            }
        }
    }
}



@Composable
private fun ShopScreenContent(
    listState: LazyListState,
    viewModel: ShopViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(vertical = 16.dp)
            .fillMaxSize()
    ) {
        EditableTextField(
            text = viewModel.shop.value.name,
            onTextChange = { viewModel.shop.value.name = it },
            label = stringResource(R.string.shop_placeholder),
            imeAction = ImeAction.Done,
            enabled = viewModel.isEditing.value,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        HorizontalDivider(Modifier.padding(horizontal = 8.dp))

        val cashbacksState = viewModel.cashbacksLiveData.observeAsState()

        Crossfade(
            targetState = cashbacksState.value,
            label = "cashback cross-fade",
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        ) { cashbacks ->
            when (cashbacks) {
                null -> LoadingInBox()
                else -> {
                    if (cashbacks.isEmpty()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = stringResource(R.string.empty_cashbacks_list),
                                color = MaterialTheme.colorScheme.onBackground.animate(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(cashbacks) {
                                CashbackComposable(
                                    cashback = it,
                                    onClick = {},
                                    onEdit = {},
                                    onDelete = {}
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}