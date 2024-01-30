package com.cashbacks.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.rounded.DataArray
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.cashbacks.app.R
import com.cashbacks.app.ui.composables.CashbackComposable
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.DisposableEffectWithLifecycle
import com.cashbacks.app.ui.composables.EditableTextField
import com.cashbacks.app.ui.composables.EmptyList
import com.cashbacks.app.ui.composables.InfoScreenTopAppBar
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.ui.screens.navigation.AppScreens
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.viewmodel.ShopViewModel

@Composable
fun ShopScreen(
    viewModel: ShopViewModel,
    popBackStack: () -> Unit,
    navigateTo: (route: String) -> Unit
) {
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

    Crossfade(
        targetState = viewModel.state.value,
        label = "content loading animation",
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        modifier = Modifier
            .imePadding()
            .fillMaxSize()
    ) { state ->
        when (state) {
            ViewModelState.Loading -> LoadingInBox()
            ViewModelState.Editing, ViewModelState.Viewing -> ShopScreenContent(
                viewModel = viewModel,
                popBackStack = popBackStack,
                navigateTo = navigateTo
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShopScreenContent(
    viewModel: ShopViewModel,
    popBackStack: () -> Unit,
    navigateTo: (route: String) -> Unit
) {
    val listState = rememberLazyListState()
    val listItemsCount = remember {
        derivedStateOf { listState.layoutInfo.totalItemsCount }
    }


    CollapsingToolbarScaffold(
        topBar = {
            InfoScreenTopAppBar(
                title = stringResource(AppScreens.Shop.titleRes),
                isInEdit = remember {
                    derivedStateOf { viewModel.state.value == ViewModelState.Editing }
                },
                isLoading = remember {
                    derivedStateOf { viewModel.state.value == ViewModelState.Loading }
                },
                onEdit = viewModel::edit,
                onSave = viewModel::save,
                onDelete = viewModel::deleteShop,
                onBack = popBackStack
            )
        },
        floatingActionButtons = {
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
                        navigateTo(
                            AppScreens.Cashback.createUrlFromShop(
                                id = null,
                                shopId = viewModel.shopId
                            )
                        )
                    },
                    expanded = !listState.canScrollForward && listItemsCount.value > 0
                )
            }
        },
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(vertical = 16.dp)
                .padding(contentPadding)
                .fillMaxSize()
        ) {
            EditableTextField(
                text = viewModel.shop.value.name,
                onTextChange = { viewModel.shop.value.name = it },
                label = stringResource(R.string.shop_placeholder),
                imeAction = ImeAction.Done,
                enabled = viewModel.state.value == ViewModelState.Editing,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            HorizontalDivider(Modifier.padding(horizontal = 8.dp))

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