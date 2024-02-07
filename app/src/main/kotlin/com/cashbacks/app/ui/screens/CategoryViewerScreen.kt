package com.cashbacks.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cashbacks.app.R
import com.cashbacks.app.ui.composables.BasicFloatingActionButton
import com.cashbacks.app.ui.composables.BasicInfoCashback
import com.cashbacks.app.ui.composables.CashbackComposable
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.ConfirmDeletionDialog
import com.cashbacks.app.ui.composables.EditDeleteContent
import com.cashbacks.app.ui.composables.PrimaryListContentTabPage
import com.cashbacks.app.ui.composables.PrimaryTabsLayout
import com.cashbacks.app.ui.composables.ScrollableListItem
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ScreenEvents
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.ui.managment.rememberScrollableListItemState
import com.cashbacks.app.ui.screens.navigation.AppScreens
import com.cashbacks.app.util.Loading
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.viewmodel.CategoryViewerViewModel
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Shop
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CategoryViewerScreen(
    viewModel: CategoryViewerViewModel,
    navigateTo: (route: String) -> Unit,
    popBackStack: () -> Unit
) {
    BackHandler(onBack = popBackStack)

    val tabPages = arrayOf(
        AppScreens.Shop,
        AppScreens.Cashback
    )
    val pagerState = rememberPagerState { tabPages.size }
    val snackbarState = remember(::SnackbarHostState)
    val scope = rememberCoroutineScope()

    val showSnackbar = remember {
        fun(message: String) {
            scope.launch { if (message.isNotBlank()) snackbarState.showSnackbar(message) }
        }
    }

    var dialogType: DialogType? by rememberSaveable { mutableStateOf(null) }
    LaunchedEffect(key1 = true) {
        viewModel.eventsFlow.collect { event ->
            when (event) {
                is ScreenEvents.Navigate -> event.route?.let(navigateTo) ?: popBackStack()
                is ScreenEvents.ShowSnackbar -> showSnackbar(event.message)
                is ScreenEvents.OpenDialog -> dialogType = event.type
                ScreenEvents.CloseDialog -> dialogType = null
            }
        }
    }


    if (dialogType is DialogType.ConfirmDeletion<*>) {
        val value = (dialogType as DialogType.ConfirmDeletion<*>).value
        ConfirmDeletionDialog(
            text = when (value) {
                is Shop -> stringResource(R.string.confirm_shop_deletion, value.name)
                is Cashback -> stringResource(R.string.confrim_cashback_deletion)
                else -> ""
            },
            onConfirm = {
                when (value) {
                    is Shop -> viewModel.deleteShop(value)
                    is Cashback -> viewModel.deleteCashback(value)
                }
            },
            onDismiss = viewModel::closeDialog
        )
    }


    CollapsingToolbarScaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    when (viewModel.state.value) {
                        ViewModelState.Loading -> Loading(
                            color = MaterialTheme.colorScheme.onPrimary.animate(),
                            modifier = Modifier.scale(.6f)
                        )
                        else -> Text(
                            text = viewModel.category.value.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = popBackStack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = "return to previous screen",
                            modifier = Modifier.scale(1.2f)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.showSnackbar("Поиск") },
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "search",
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
            Crossfade(
                targetState = pagerState.currentPage,
                label = "fab anim",
                animationSpec = tween(durationMillis = 200, easing = LinearEasing)
            ) { pageIndex ->
                BasicFloatingActionButton(
                    icon = when (tabPages[pageIndex]) {
                        AppScreens.Cashback -> Icons.Rounded.Add
                        AppScreens.Shop -> Icons.Rounded.Edit
                    },
                    onClick = {
                        viewModel.onItemClick {
                            when (tabPages[pageIndex]) {
                                AppScreens.Cashback -> viewModel.navigateTo(
                                    AppScreens.Cashback.createUrlFromCategory(
                                        id = null,
                                        categoryId = viewModel.categoryId
                                    )
                                )
                                AppScreens.Shop -> viewModel.navigateTo(
                                    AppScreens.CategoryEditor.createUrl(viewModel.categoryId)
                                )
                            }
                        }
                    }
                )
            }
        },
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
        modifier = Modifier.fillMaxSize()
    ) { contentPadding ->
        Crossfade(
            targetState = viewModel.state.value,
            label = "loading anim",
            animationSpec = tween(durationMillis = 100, easing = LinearEasing),
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) { state ->
            when (state) {
                ViewModelState.Loading -> LoadingInBox()
                else -> CategoryViewerContent(viewModel, pagerState, tabPages)
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryViewerContent(
    viewModel: CategoryViewerViewModel,
    pagerState: PagerState,
    tabPages: Array<AppScreens.TabPages>,
    modifier: Modifier = Modifier
) {


    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface.animate())
            .then(modifier)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PrimaryTabsLayout(
            pagerState = pagerState,
            pages = tabPages
        ) { page ->
            PrimaryListContentTabPage(
                items = when (page) {
                    AppScreens.Shop -> viewModel.shopsLiveData.observeAsState().value
                    AppScreens.Cashback -> viewModel.cashbacksLiveData.observeAsState().value
                },
                placeholderText = when (page) {
                    AppScreens.Cashback -> stringResource(R.string.empty_cashbacks_list)
                    AppScreens.Shop -> stringResource(R.string.empty_shops_list_viewing)
                },
            ) {
                when (it) {
                    is Shop -> ShopComposable(
                        shop = it,
                        onClick = {
                            viewModel.navigateTo(
                                AppScreens.Shop.createUrl(
                                    categoryId = viewModel.categoryId,
                                    shopId = it.id
                                )
                            )
                        },
                        onEdit = {
                            viewModel.navigateTo(
                                AppScreens.Shop.createUrl(
                                    categoryId = viewModel.categoryId,
                                    shopId = it.id,
                                    isEdit = true
                                )
                            )
                        },
                        onDelete = { viewModel.openDialog(DialogType.ConfirmDeletion(it)) },
                        isInEdit = false
                    )

                    is Cashback -> CashbackComposable(
                        cashback = it,
                        onClick = {
                            viewModel.navigateTo(
                                AppScreens.Cashback.createUrlFromCategory(
                                    id = it.id,
                                    categoryId = viewModel.categoryId
                                )
                            )
                        },
                        onDelete = { viewModel.openDialog(DialogType.ConfirmDeletion(it)) }
                    )
                }
            }
        }
    }
}


@Composable
internal fun ShopComposable(
    shop: Shop,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isInEdit: Boolean
) {
    val listItemState = rememberScrollableListItemState()
    val scope = rememberCoroutineScope()

    ScrollableListItem(
        onClick = if (isInEdit) null else onClick,
        hiddenContent = {
            EditDeleteContent(
                onEditClick = remember {
                    fun () {
                        onEdit()
                        scope.launch { listItemState.swipe() }
                    }
                },
                onDeleteClick = remember {
                    fun () {
                        onDelete()
                        scope.launch { listItemState.swipe() }
                    }
                }
            )
        }
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = shop.name,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            supportingContent = {
                if (shop.maxCashback == null) {
                    Text(
                        text = stringResource(R.string.no_cashbacks_for_shop),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            trailingContent = {
                shop.maxCashback?.let { BasicInfoCashback(cashback = it) }
            },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.background.animate(),
                supportingColor = MaterialTheme.colorScheme.error.animate(),
                trailingIconColor = MaterialTheme.colorScheme.primary.animate()
            )
        )
    }
}
