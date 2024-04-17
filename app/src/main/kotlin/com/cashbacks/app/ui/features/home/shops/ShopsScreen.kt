package com.cashbacks.app.ui.features.home.shops

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DataArray
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cashbacks.app.R
import com.cashbacks.app.ui.composables.BasicFloatingActionButton
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.ConfirmDeletionDialog
import com.cashbacks.app.ui.composables.EmptyList
import com.cashbacks.app.ui.composables.ShopComposable
import com.cashbacks.app.ui.features.home.HomeTopAppBar
import com.cashbacks.app.ui.features.home.HomeTopAppBarState
import com.cashbacks.app.ui.features.shop.ShopArgs
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ListState
import com.cashbacks.app.ui.managment.ScreenEvents
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.keyboardAsState
import com.cashbacks.app.util.reversed
import com.cashbacks.domain.model.Shop
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ShopsScreen(
    viewModel: ShopsViewModel,
    title: String,
    bottomPadding: Dp,
    openDrawer: () -> Unit,
    navigateToShop: (args: ShopArgs) -> Unit,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        when {
            viewModel.isEditing.value -> viewModel.isEditing.value = false
            else -> viewModel.navigateTo(null)
        }
    }

    val topAppBarState = rememberTopAppBarState()
    val lazyListState = rememberLazyListState()
    val snackbarHostState = remember(::SnackbarHostState)
    val scope = rememberCoroutineScope()
    val keyboardState = keyboardAsState()

    val showSnackbar = remember {
        fun(message: String) {
            scope.launch { snackbarHostState.showSnackbar(message) }
        }
    }

    var dialogType: DialogType? by rememberSaveable { mutableStateOf(null) }
    LaunchedEffect(key1 = true) {
        viewModel.eventsFlow.collect { event ->
            when (event) {
                is ScreenEvents.OpenDialog -> dialogType = event.type
                ScreenEvents.CloseDialog -> dialogType = null
                is ScreenEvents.Navigate -> (event.args as? ShopArgs)
                    ?.let(navigateToShop)
                    ?: popBackStack()
                is ScreenEvents.ShowSnackbar -> event.message.let(showSnackbar)
            }
        }
    }

    dialogType.takeIf { it is DialogType.ConfirmDeletion<*> }?.let { type ->
        val shop = (type as DialogType.ConfirmDeletion<*>).value as Shop
        ConfirmDeletionDialog(
            text = stringResource(R.string.confirm_shop_deletion, shop.name),
            onConfirm = { viewModel.deleteShop(shop) },
            onClose = viewModel::closeDialog
        )
    }

    val fabPaddingDp = rememberSaveable { mutableFloatStateOf(0f) }

    CollapsingToolbarScaffold(
        topBar = {
            HomeTopAppBar(
                title = title,
                query = viewModel.query.value,
                onQueryChange = viewModel.query::value::set,
                state = viewModel.appBarState,
                onStateChange = viewModel::appBarState::set,
                searchPlaceholder = stringResource(R.string.search_shops_placeholder),
                onNavigationIconClick = openDrawer
            )
        },
        topBarState = topAppBarState,
        contentState = lazyListState,
        topBarContainerColor = when (viewModel.appBarState) {
            HomeTopAppBarState.Search -> Color.Unspecified
            HomeTopAppBarState.TopBar -> MaterialTheme.colorScheme.primary
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    containerColor = MaterialTheme.colorScheme.background.reversed.animate(),
                    contentColor = MaterialTheme.colorScheme.onBackground.reversed.animate(),
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        floatingActionButtons = {
            AnimatedVisibility(visible = !keyboardState.value) {
                BasicFloatingActionButton(
                    icon = when {
                        viewModel.isEditing.value -> Icons.Rounded.EditOff
                        else -> Icons.Rounded.Edit
                    },
                    onClick = {
                        viewModel.onItemClick {
                            viewModel.isEditing.value = !viewModel.isEditing.value
                        }
                    }
                )
            }
        },
        fabModifier = Modifier
            .windowInsetsPadding(WindowInsets.tappableElement.only(WindowInsetsSides.End))
            .padding(bottom = bottomPadding)
            .graphicsLayer {
                fabPaddingDp.floatValue = size.height.toDp().value
            },
        contentWindowInsets = WindowInsets.ime.only(WindowInsetsSides.Bottom),
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
    ) {
        Crossfade(
            targetState = viewModel.state.value,
            label = "loading animation",
            animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
            modifier = Modifier.fillMaxSize()
        ) { state ->
            when (state) {
                ListState.Loading -> LoadingInBox(
                    modifier = Modifier.padding(bottom = bottomPadding)
                )
                ListState.Empty -> EmptyList(
                    text = when {
                        !viewModel.isEditing.value -> stringResource(R.string.empty_shops_list_viewing)
                        viewModel.isSearch -> {
                            when {
                                viewModel.query.value.isBlank() -> stringResource(R.string.empty_search_query)
                                else -> stringResource(R.string.empty_search_results)
                            }
                        }
                        else -> stringResource(R.string.empty_shops)
                    },
                    icon = Icons.Rounded.DataArray,
                    iconModifier = Modifier.scale(2.5f),
                    modifier = Modifier
                        .padding(bottom = bottomPadding)
                        .fillMaxSize()
                )
                ListState.Stable -> ShopsList(
                    viewModel = viewModel,
                    state = lazyListState,
                    bottomPadding = (bottomPadding + fabPaddingDp.floatValue.dp).animate()
                )
            }
        }
    }
}



@Composable
private fun ShopsList(
    viewModel: ShopsViewModel,
    state: LazyListState,
    bottomPadding: Dp
) {
    LazyColumn(
        state = state,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(viewModel.shops) { index, shop ->
            ShopComposable(
                categoryShop = shop,
                isEditing = viewModel.isEditing.value,
                isSwiped = viewModel.selectedShopIndex == index,
                onSwipe = { isSwiped ->
                    viewModel.selectedShopIndex = when {
                        isSwiped -> index
                        else -> null
                    }
                },
                onClick = {
                    viewModel.onItemClick {
                        viewModel.selectedShopIndex = -1
                        viewModel.navigateTo(
                            ShopArgs(shopId = shop.id, isEditing = false)
                        )
                    }
                },
                onEdit = {
                    viewModel.onItemClick {
                        viewModel.selectedShopIndex = -1
                        viewModel.navigateTo(
                            ShopArgs(shopId = shop.id, isEditing = true)
                        )
                    }
                },
                onDelete = {
                    viewModel.onItemClick {
                        viewModel.selectedShopIndex = -1
                        viewModel.openDialog(DialogType.ConfirmDeletion(shop.asShop()))
                    }
                }
            )
        }

        item {
            Spacer(Modifier.height(bottomPadding))
        }
    }
}