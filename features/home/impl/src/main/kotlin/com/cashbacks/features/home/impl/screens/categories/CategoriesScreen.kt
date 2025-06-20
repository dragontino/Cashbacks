package com.cashbacks.features.home.impl.screens.categories

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DataArray
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cashbacks.common.composables.BasicFloatingActionButton
import com.cashbacks.common.composables.BoundedSnackbar
import com.cashbacks.common.composables.CollapsingToolbarScaffold
import com.cashbacks.common.composables.ConfirmDeletionDialog
import com.cashbacks.common.composables.EmptyList
import com.cashbacks.common.composables.LoadingInBox
import com.cashbacks.common.composables.NewNameTextField
import com.cashbacks.common.composables.management.DialogType
import com.cashbacks.common.composables.management.ListState
import com.cashbacks.common.composables.management.ViewModelState
import com.cashbacks.common.composables.management.toListState
import com.cashbacks.common.composables.theme.CashbacksTheme
import com.cashbacks.common.composables.utils.animate
import com.cashbacks.common.composables.utils.floatingActionButtonEnterAnimation
import com.cashbacks.common.composables.utils.floatingActionButtonExitAnimation
import com.cashbacks.common.composables.utils.keyboardAsState
import com.cashbacks.common.composables.utils.loadingContentAnimationSpec
import com.cashbacks.common.composables.utils.mix
import com.cashbacks.common.composables.utils.reversed
import com.cashbacks.common.composables.utils.smoothScrollToItem
import com.cashbacks.common.resources.R
import com.cashbacks.features.cashback.domain.utils.asCashbackOwner
import com.cashbacks.features.cashback.presentation.api.CashbackArgs
import com.cashbacks.features.cashback.presentation.api.composables.MaxCashbackOwnerComposable
import com.cashbacks.features.category.domain.model.Category
import com.cashbacks.features.category.presentation.api.CategoryArgs
import com.cashbacks.features.home.impl.composables.HomeAppBarDefaults
import com.cashbacks.features.home.impl.composables.HomeTopAppBar
import com.cashbacks.features.home.impl.composables.HomeTopAppBarState
import com.cashbacks.features.home.impl.mvi.CategoriesIntent
import com.cashbacks.features.home.impl.mvi.CategoriesLabel
import com.cashbacks.features.home.impl.mvi.CategoriesState
import com.cashbacks.features.home.impl.navigation.HomeDestination
import com.cashbacks.features.home.impl.utils.copy
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Stable
@Composable
internal fun CategoriesRoot(
    openDrawer: () -> Unit,
    navigateToCategory: (args: CategoryArgs) -> Unit,
    navigateToCashback: (args: CashbackArgs) -> Unit,
    navigateBack: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = koinViewModel()
) {
    val state by viewModel.stateFlow.collectAsState()
    val snackbarHostState = remember(::SnackbarHostState)
    val contentState = rememberLazyListState()
    val dialogType = rememberSaveable { mutableStateOf<DialogType?>(null) }

    LaunchedEffect(Unit) {
        viewModel.labelFlow.collect { label ->
            when (label) {
                is CategoriesLabel.NavigateBack -> navigateBack()
                is CategoriesLabel.NavigateToCategory -> navigateToCategory(label.args)
                is CategoriesLabel.NavigateToCashback -> navigateToCashback(label.args)
                is CategoriesLabel.OpenNavigationDrawer -> openDrawer()
                is CategoriesLabel.DisplayMessage -> launch {
                    snackbarHostState.showSnackbar(label.message)
                }
                is CategoriesLabel.ChangeOpenedDialog -> dialogType.value = label.type
                is CategoriesLabel.ScrollToEnd -> launch {
                    delay(700)
                    contentState.smoothScrollToItem(contentState.layoutInfo.totalItemsCount)
                }
            }
        }
    }

    if (dialogType.value is DialogType.ConfirmDeletion<*>) {
        val category = (dialogType.value as DialogType.ConfirmDeletion<*>).value as Category
        ConfirmDeletionDialog(
            text = stringResource(
                R.string.confirm_category_deletion,
                category.name
            ),
            onConfirm = { viewModel.sendIntent(CategoriesIntent.DeleteCategory(category)) },
            onClose = { viewModel.sendIntent(CategoriesIntent.CloseDialog) }
        )
    }

    CategoriesScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        contentState = contentState,
        contentPadding = contentPadding,
        sendIntent = viewModel::sendIntent,
        modifier = modifier
    )
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CategoriesScreen(
    state: CategoriesState,
    snackbarHostState: SnackbarHostState,
    contentState: LazyListState,
    contentPadding: PaddingValues,
    sendIntent: (CategoriesIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        val intent = when (state.viewModelState) {
            ViewModelState.Editing -> CategoriesIntent.FinishEdit
            ViewModelState.Viewing -> CategoriesIntent.ClickButtonBack
        }
        sendIntent(intent)
    }


    val topBarState = rememberTopAppBarState()
    val keyboardState = keyboardAsState()
    val layoutDirection = LocalLayoutDirection.current

    LaunchedEffect(keyboardState) {
        if (keyboardState.value.not()) {
            sendIntent(CategoriesIntent.FinishCreatingCategory)
        }
    }


    val fabHeightPx = rememberSaveable { mutableFloatStateOf(0f) }

    Box(contentAlignment = Alignment.Center) {
        CollapsingToolbarScaffold(
            topBar = {
                HomeTopAppBar(
                    title = HomeDestination.Categories.screenTitle,
                    state = state.appBarState,
                    onStateChange = {
                         sendIntent(CategoriesIntent.ChangeAppBarState(it))
                    },
                    searchPlaceholder = stringResource(R.string.search_categories_placeholder),
                    onNavigationIconClick = { sendIntent(CategoriesIntent.ClickNavigationButton) },
                    colors = HomeAppBarDefaults.colors(
                        topBarContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = .6f)
                            .mix(MaterialTheme.colorScheme.primary)
                            .ratio(topBarState.overlappedFraction),
                    ),
                )
            },
            topBarState = topBarState,
            contentState = contentState,
            topBarScrollEnabled = state.appBarState is HomeTopAppBarState.TopBar,
            snackbarHost = {
                SnackbarHost(snackbarHostState) {
                    BoundedSnackbar(
                        snackbarData = it,
                        containerColor = MaterialTheme.colorScheme.background.reversed.animate(),
                        contentColor = MaterialTheme.colorScheme.onBackground.reversed.animate(),
                        shape = MaterialTheme.shapes.medium,
                    )
                }
            },
            floatingActionButtons = {
                AnimatedVisibility(
                    visible = state.viewModelState == ViewModelState.Editing
                            && state.appBarState !is HomeTopAppBarState.Search
                            && !state.isCreatingCategory,
                    enter = floatingActionButtonEnterAnimation(),
                    exit = floatingActionButtonExitAnimation(),
                ) {
                    BasicFloatingActionButton(icon = Icons.Rounded.Add) {
                        sendIntent(CategoriesIntent.StartCreatingCategory)
                        sendIntent(CategoriesIntent.ScrollToEnd)
                    }
                }
                AnimatedVisibility(visible = !state.isCreatingCategory && !keyboardState.value) {
                    BasicFloatingActionButton(
                        icon = when (state.viewModelState) {
                            ViewModelState.Editing -> Icons.Rounded.EditOff
                            ViewModelState.Viewing -> Icons.Rounded.Edit
                        },
                        onClick = { sendIntent(CategoriesIntent.SwitchEdit) },
                    )
                }
            },
            contentWindowInsets = WindowInsets.ime.only(WindowInsetsSides.Bottom),
            fabModifier = Modifier
                .onGloballyPositioned { fabHeightPx.floatValue = it.size.height.toFloat() }
                .windowInsetsPadding(WindowInsets.tappableElement.only(WindowInsetsSides.End))
                .padding(bottom = contentPadding.calculateBottomPadding()),
            modifier = modifier.fillMaxSize(),
        ) {
            CategoriesList(
                state = state,
                lazyListState = contentState,
                contentPadding = contentPadding.copy(layoutDirection) {
                    copy(
                        bottom = with(LocalDensity.current) {
                            (bottom + fabHeightPx.floatValue.toDp())
                        }
                    )
                },
                sendIntent = sendIntent
            )
        }


        AnimatedVisibility(
            visible = state.isCreatingCategory,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                expandFrom = Alignment.Bottom
            ),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                shrinkTowards = Alignment.Bottom
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            NewNameTextField(placeholder = stringResource(R.string.category_placeholder)) { name ->
                 sendIntent(CategoriesIntent.AddCategory(name))
            }
        }
    }
}



@Composable
private fun CategoriesList(
    state: CategoriesState,
    lazyListState: LazyListState,
    contentPadding: PaddingValues,
    sendIntent: (CategoriesIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Crossfade(
        targetState = state.categories?.toList().toListState(),
        label = "Content loading animation",
        animationSpec = loadingContentAnimationSpec(),
        modifier = modifier
    ) { categoriesListState ->
        when (categoriesListState) {
            is ListState.Loading -> {
                LoadingInBox(
                    modifier = Modifier.padding(contentPadding)
                )
            }

            is ListState.Empty -> {
                Crossfade(
                    targetState = state.appBarState to state.viewModelState,
                    label = "emptyList",
                    animationSpec = loadingContentAnimationSpec()
                ) { (appBarState, viewModelState) ->
                    EmptyList(
                        text = when (appBarState) {
                            is HomeTopAppBarState.Search -> {
                                when {
                                    appBarState.query.isBlank() -> stringResource(R.string.empty_search_query)
                                    else -> stringResource(R.string.empty_search_results)
                                }
                            }

                            is HomeTopAppBarState.TopBar -> {
                                when (viewModelState) {
                                    ViewModelState.Viewing -> stringResource(R.string.empty_categories_list_viewing)
                                    ViewModelState.Editing -> stringResource(R.string.empty_categories_list_editing)
                                }
                            }
                        },
                        icon = Icons.Rounded.DataArray,
                        iconModifier = Modifier.scale(2.5f),
                        modifier = Modifier
                            .padding(contentPadding)
                            .fillMaxSize()
                    )
                }
            }

            is ListState.Stable -> {
                LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    contentPadding.calculateTopPadding().takeIf { it.value > 0 }?.let {
                        item {
                            Spacer(Modifier.height(it))
                        }
                    }

                    itemsIndexed(categoriesListState.data) { index, (category, cashbacks) ->
                        MaxCashbackOwnerComposable(
                            cashbackOwner = category.asCashbackOwner(),
                            maxCashbacks = cashbacks.toImmutableSet(),
                            isEditing = state.viewModelState == ViewModelState.Editing,
                            isSwiped = state.selectedCategoryIndex == index,
                            onSwipe = { isSwiped ->
                                sendIntent(
                                    CategoriesIntent.SwipeCategory(
                                        position = index,
                                        isSwiped = isSwiped
                                    )
                                )
                            },
                            onClick = {
                                sendIntent(CategoriesIntent.SwipeCategory(null))
                                sendIntent(
                                    CategoriesIntent.NavigateToCategory(CategoryArgs.Viewing(id = category.id))
                                )
                            },
                            onClickToCashback = { cashback ->
                                sendIntent(
                                    CategoriesIntent.NavigateToCashback(
                                        CashbackArgs.fromCategory(
                                            cashbackId = cashback.id,
                                            categoryId = category.id
                                        )
                                    )
                                )
                            },
                            onEdit = {
                                sendIntent(
                                    CategoriesIntent.SwipeCategory(null)
                                )
                                sendIntent(
                                    CategoriesIntent.NavigateToCategory(
                                        args = CategoryArgs.Editing(id = category.id),
                                    )
                                )
                            },
                            onDelete = {
                                sendIntent(CategoriesIntent.SwipeCategory(null))

                                sendIntent(
                                    CategoriesIntent.OpenDialog(DialogType.ConfirmDeletion(category))
                                )
                            },
                            modifier = Modifier.padding(
                                start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                                end = contentPadding.calculateEndPadding(LocalLayoutDirection.current)
                            )
                        )
                    }

                    contentPadding.calculateBottomPadding().takeIf { it.value > 0 }?.let {
                        item {
                            Spacer(modifier = Modifier.height(it))
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun CategoriesScreenPreview() {
    CashbacksTheme(isDarkTheme = false) {
        CategoriesScreen(
            state = CategoriesState(
                categories = buildMap {
                    for (i in 1..5) {
                        val category = Category(id = i.toLong(), name = "Test Name")
                        this[category] = emptySet()
                    }
                }
            ),
            snackbarHostState = remember(::SnackbarHostState),
            contentState = rememberLazyListState(),
            contentPadding = PaddingValues(0.dp),
            sendIntent = {}
        )
    }
}