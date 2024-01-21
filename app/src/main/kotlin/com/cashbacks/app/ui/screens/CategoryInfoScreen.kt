package com.cashbacks.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cashbacks.app.R
import com.cashbacks.app.ui.composables.BasicInfoCashback
import com.cashbacks.app.ui.composables.CashbackComposable
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.DisposableEffectWithLifecycle
import com.cashbacks.app.ui.composables.EditDeleteContent
import com.cashbacks.app.ui.composables.EditableTextField
import com.cashbacks.app.ui.composables.InfoScreenTopAppBar
import com.cashbacks.app.ui.composables.ModalSheetDefaults
import com.cashbacks.app.ui.composables.NewNameTextField
import com.cashbacks.app.ui.composables.ScrollableListItem
import com.cashbacks.app.ui.screens.navigation.AppScreens
import com.cashbacks.app.ui.theme.CashbacksTheme
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.keyboardAsState
import com.cashbacks.app.viewmodel.CategoryInfoViewModel
import com.cashbacks.app.viewmodel.CategoryInfoViewModel.ViewModelState
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Shop
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun CategoryInfoScreen(
    viewModel: CategoryInfoViewModel,
    navigateTo: (route: String) -> Unit,
    popBackStack: () -> Unit
) {
    val tabPages = arrayOf(
        AppScreens.Shop,
        AppScreens.Cashback
    )
    val pagerState = rememberPagerState { tabPages.size }
    val snackbarState = remember(::SnackbarHostState)
    val scope = rememberCoroutineScope()

    val keyboardIsOpen = keyboardAsState()
    LaunchedEffect(Unit) {
        snapshotFlow { keyboardIsOpen.value }
            .collectLatest { isKeyboardOpen ->
                if (!isKeyboardOpen) {
                    viewModel.addingShopState.value = false
                }
            }
    }

    val showSnackbar = { message: String ->
        scope.launch { snackbarState.showSnackbar(message) }
    }


    DisposableEffectWithLifecycle(
        onDestroy = viewModel::saveCategory
    )

    BackHandler {
        if (viewModel.isEditing.value) {
            viewModel.saveCategory()
            viewModel.isEditing.value = false
        } else {
            popBackStack()
        }
    }


    CollapsingToolbarScaffold(
        topBar = {
            InfoScreenTopAppBar(
                title = stringResource(AppScreens.Category.titleRes),
                isInEdit = viewModel.isEditing,
                isLoading = remember {
                    derivedStateOf { viewModel.categoryState.value == ViewModelState.Loading }
                },
                onEdit = { viewModel.isEditing.value = true },
                onSave = {
                    viewModel.saveCategory()
                    viewModel.isEditing.value = false
                },
                onDelete = viewModel::deleteCategory,
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
                        val tabTitle = stringResource(tabPages[pagerState.currentPage].titleRes)
                        Text(
                            text = stringResource(R.string.add_item, tabTitle),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    icon = {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                    },
                    onClick = {
                        when (tabPages[pagerState.currentPage]) {
                            AppScreens.Shop -> viewModel.addingShopState.value = true
                            AppScreens.Cashback -> navigateTo(AppScreens.Cashback.createUrl(null))
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer.animate(),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer.animate(),
                    elevation = FloatingActionButtonDefaults.loweredElevation()
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarState) {
                Snackbar(
                    snackbarData = it,
                    containerColor = MaterialTheme.colorScheme.onBackground.animate(),
                    contentColor = MaterialTheme.colorScheme.background.animate(),
                    actionColor = MaterialTheme.colorScheme.primary.animate()
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { contentPadding ->

        Crossfade(
            targetState = viewModel.categoryState.value,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            label = "category_info_animation",
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) { viewModelState ->

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (viewModelState) {
                    ViewModelState.Loading -> LoadingInBox(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    )
                    ViewModelState.Ready -> CategoryInfoScreenContent(
                        viewModel = viewModel,
                        pagerState = pagerState,
                        tabPages = tabPages,
                        navigateTo = navigateTo,
                        showSnackbar = { showSnackbar(it) }
                    )
                }

                AnimatedVisibility(
                    visible = viewModelState != ViewModelState.Loading && viewModel.addingShopState.value,
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
                        viewModel.addShop(name)
                        viewModel.addingShopState.value = false
                    }
                }
            }
        }
    }
}


@ExperimentalFoundationApi
@Composable
private fun CategoryInfoScreenContent(
    viewModel: CategoryInfoViewModel,
    pagerState: PagerState,
    tabPages: Array<AppScreens.TabPages>,
    navigateTo: (route: String) -> Unit,
    showSnackbar: (message: String) -> Unit
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface.animate())
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        EditableTextField(
            text = viewModel.category.value.name,
            onTextChange = viewModel.category.value::name::set,
            readOnly = !viewModel.isEditing.value,
            label = stringResource(R.string.category_placeholder),
            imeAction = ImeAction.Done,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        )

        TabsLayout(
            pagerState = pagerState,
            pages = tabPages,
            scrollEnabled = !viewModel.addingShopState.value,
            modifier = Modifier
                .shadow(elevation = 20.dp, shape = ModalSheetDefaults.BottomSheetShape)
                .background(MaterialTheme.colorScheme.background.animate())
                .padding(top = 8.dp)
                .padding(horizontal = 8.dp)
                .clip(ModalSheetDefaults.BottomSheetShape)
        ) { page ->

            TabPage(
                items = when (page) {
                    AppScreens.Shop -> viewModel.shopsLiveData.observeAsState(listOf())
                    AppScreens.Cashback -> viewModel.cashbacksLiveData.observeAsState(listOf())
                },
                vmState = when (page) {
                    AppScreens.Shop -> viewModel.shopsState
                    AppScreens.Cashback -> viewModel.cashbacksState
                },
                placeholderText = when (page) {
                    AppScreens.Cashback -> stringResource(R.string.empty_cashbacks_list)
                    AppScreens.Shop -> stringResource(R.string.empty_shops_list)
                }
            ) {
                when (it) {
                    is Shop -> ShopComposable(
                        shop = it,
                        onClick = {
                            navigateTo(
                                AppScreens.Shop.createUrl(
                                    categoryId = viewModel.categoryId,
                                    shopId = it.id
                                )
                            )
                        },
                        onEdit = {
                            navigateTo(
                                AppScreens.Shop.createUrl(
                                    categoryId = viewModel.categoryId,
                                    shopId = it.id,
                                    isEdit = true
                                )
                            )
                        },
                        onDelete = { viewModel.deleteShop(it, showSnackbar) },
                        isInEdit = viewModel.isEditing.value
                    )
                    is Cashback -> CashbackComposable(
                        cashback = it,
                        onClick = { navigateTo(AppScreens.Cashback.createUrl(it.id)) },
                        onEdit = { navigateTo(AppScreens.Cashback.createUrl(it.id, isEdit = true)) },
                        onDelete = { viewModel.deleteCashback(it, showSnackbar) }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TabsLayout(
    pages: Array<AppScreens.TabPages>,
    modifier: Modifier = Modifier,
    scrollEnabled: Boolean = true,
    pagerState: PagerState = rememberPagerState { pages.size },
    content: @Composable ((page: AppScreens.TabPages) -> Unit)
) {

    val scope = rememberCoroutineScope()
    val selectedTabIndex by remember {
        derivedStateOf { pagerState.currentPage }
    }

    val scrollToPage = { index: Int ->
        scope.launch {
            pagerState.animateScrollToPage(
                page = index,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
    ) {
        SecondaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground.animate(),
            modifier = Modifier.fillMaxWidth()
        ) {
            pages.forEachIndexed { index, page ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { scrollToPage(index) },
                    text = {
                        Text(
                            text = stringResource(page.tabTitleRes),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    enabled = scrollEnabled || selectedTabIndex == index,
                    selectedContentColor = MaterialTheme.colorScheme.primary.animate(),
                    unselectedContentColor = MaterialTheme.colorScheme.outline.animate()
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            pageSpacing = 8.dp,
            userScrollEnabled = scrollEnabled,
            modifier = Modifier.fillMaxWidth(),
            pageContent = { page -> content(pages[page]) }
        )
    }
}



@Composable
private fun <T> TabPage(
    items: State<List<T>>,
    vmState: State<ViewModelState>,
    placeholderText: String,
    itemComposable: @Composable ((T) -> Unit)
) {
    val listState = rememberLazyListState()

    Crossfade(
        targetState = vmState.value,
        label = "tabItems"
    ) { state ->
        when (state) {
            ViewModelState.Loading -> LoadingInBox()
            ViewModelState.Ready -> {
                if (items.value.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = placeholderText,
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
                        items(items.value, itemContent = { itemComposable(it) })
                    }
                }
            }
        }
    }
}


@Composable
private fun ShopComposable(
    shop: Shop,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isInEdit: Boolean
) {
    ScrollableListItem(
        onClick = if (isInEdit) null else onClick,
        hiddenContent = {
            EditDeleteContent(onEditClick = onEdit, onDeleteClick = onDelete)
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


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
private fun TabsLayoutPreview() {
    CashbacksTheme(isDarkTheme = false) {
        TabsLayout(
            pages = arrayOf(AppScreens.Shop, AppScreens.Cashback),
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Preview")
            }
        }
    }
}
