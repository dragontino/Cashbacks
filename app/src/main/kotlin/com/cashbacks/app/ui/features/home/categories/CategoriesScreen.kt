package com.cashbacks.app.ui.features.home.categories

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
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cashbacks.app.ui.composables.BasicFloatingActionButton
import com.cashbacks.app.ui.composables.BasicInfoCashback
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.ConfirmDeletionDialog
import com.cashbacks.app.ui.composables.EditDeleteContent
import com.cashbacks.app.ui.composables.EmptyList
import com.cashbacks.app.ui.composables.NewNameTextField
import com.cashbacks.app.ui.composables.ScrollableListItem
import com.cashbacks.app.ui.features.category.CategoryArgs
import com.cashbacks.app.ui.features.home.HomeTopAppBar
import com.cashbacks.app.ui.features.home.HomeTopAppBarState
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ListState
import com.cashbacks.app.ui.managment.ScreenEvents
import com.cashbacks.app.ui.managment.rememberScrollableListItemState
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.floatingActionButtonEnterAnimation
import com.cashbacks.app.util.floatingActionButtonExitAnimation
import com.cashbacks.app.util.keyboardAsState
import com.cashbacks.app.util.reversed
import com.cashbacks.app.util.smoothScrollToItem
import com.cashbacks.domain.R
import com.cashbacks.domain.model.BasicCategory
import com.cashbacks.domain.model.Category
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    title: String,
    openDrawer: () -> Unit,
    navigateToCategory: (args: CategoryArgs, isEditing: Boolean) -> Unit,
    navigateBack: () -> Unit,
    bottomPadding: Dp,
    modifier: Modifier = Modifier
) {
    BackHandler {
        val action = when (viewModel.viewModelState) {
            ViewModelState.Editing -> CategoriesAction.FinishEdit
            else -> CategoriesAction.ClickButtonBack
        }
        viewModel.push(action)
    }

    val snackbarHostState = remember(::SnackbarHostState)
    val lazyListState = rememberLazyListState()
    val keyboardState = keyboardAsState()
    val dialogType = rememberSaveable { mutableStateOf<DialogType?>(null) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.onEach { event ->
            when (event) {
                is CategoriesEvent.NavigateBack -> navigateBack()
                is CategoriesEvent.NavigateToCategory -> navigateToCategory(event.args, event.isEditing)
                is CategoriesEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is CategoriesEvent.OpenDialog -> dialogType.value = event.type
                is CategoriesEvent.CloseDialog -> dialogType.value = null
                is CategoriesEvent.ScrollToEnd -> {
                    delay(700)
                    lazyListState.smoothScrollToItem(lazyListState.layoutInfo.totalItemsCount)
                }
            }
        }.launchIn(this)

        snapshotFlow { keyboardState.value }.onEach { isKeyboardOpen ->
            if (!isKeyboardOpen) {
                viewModel.push(CategoriesAction.FinishCreatingCategory)
            }
        }.launchIn(this)
    }

    if (dialogType.value is DialogType.ConfirmDeletion<*>) {
        val category = (dialogType.value as DialogType.ConfirmDeletion<*>).value as Category
        ConfirmDeletionDialog(
            text = stringResource(
                R.string.confirm_category_deletion,
                category.name
            ),
            onConfirm = { viewModel.push(CategoriesAction.DeleteCategory(category)) },
            onClose = { viewModel.push(CategoriesAction.CloseDialog) }
        )
    }

    val fabHeightPx = rememberSaveable { mutableFloatStateOf(0f) }


    Box(contentAlignment = Alignment.Center) {
        CollapsingToolbarScaffold(
            contentState = lazyListState,
            topBar = {
                HomeTopAppBar(
                    title = title,
                    query = viewModel.query.value,
                    onQueryChange = viewModel.query::value::set,
                    state = viewModel.appBarState,
                    onStateChange = viewModel::appBarState::set,
                    searchPlaceholder = stringResource(R.string.search_categories_placeholder),
                    onNavigationIconClick = openDrawer
                )
            },
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
                AnimatedVisibility(
                    visible = viewModel.viewModelState == ViewModelState.Editing
                            && viewModel.appBarState !is HomeTopAppBarState.Search
                            && !viewModel.isCreatingCategory,
                    enter = floatingActionButtonEnterAnimation(),
                    exit = floatingActionButtonExitAnimation()
                ) {
                    BasicFloatingActionButton(icon = Icons.Rounded.Add) {
                        viewModel.onItemClick {
                            viewModel.addingCategoriesState = true
                            scope.launch {
                                delay(700)
                                lazyListState.smoothScrollToItem(viewModel.categories.lastIndex)
                            }
                        }
                    }
                }
                AnimatedVisibility(visible = !viewModel.addingCategoriesState && !keyboardState.value) {
                            viewModel.push(CategoriesAction.StartCreatingCategory)
                            viewModel.push(CategoriesAction.ScrollToEnd)
                        }
                    }
                }
                AnimatedVisibility(visible = !viewModel.isCreatingCategory && !keyboardState.value) {
                    BasicFloatingActionButton(
                        icon = when (viewModel.viewModelState) {
                            ViewModelState.Editing -> Icons.Rounded.EditOff
                            ViewModelState.Viewing -> Icons.Rounded.Edit
                        },
                        onClick = {
                            viewModel.onItemClick {
                                viewModel.push(CategoriesAction.SwitchEdit)
                            }
                        }
                    )
                }
            },
            contentWindowInsets = WindowInsets.ime.only(WindowInsetsSides.Bottom),
            fabModifier = Modifier
                .onGloballyPositioned { fabHeightPx.floatValue = it.size.height.toFloat() }
                .windowInsetsPadding(WindowInsets.tappableElement.only(WindowInsetsSides.End))
                .padding(bottom = bottomPadding),
            modifier = modifier.fillMaxSize()
        ) {

            val categoriesState = viewModel.categoriesFlow.collectAsStateWithLifecycle()
            Crossfade(
                targetState = ListState.fromList(categoriesState.value),
                label = "Content loading animation",
                animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
            ) { listState ->
                when (listState) {
                    is ListState.Loading -> {
                        LoadingInBox(
                            modifier = Modifier.padding(bottom = bottomPadding)
                        )
                    }

                    is ListState.Empty -> {
                        EmptyList(
                            text = when (val appBarState = viewModel.appBarState) {
                                is HomeTopAppBarState.Search -> {
                                    when {
                                        appBarState.query.isBlank() -> stringResource(R.string.empty_search_query)
                                        else -> stringResource(R.string.empty_search_results)
                                    }
                                }

                                is HomeTopAppBarState.TopBar -> {
                                    when (viewModel.viewModelState) {
                                        ViewModelState.Viewing -> stringResource(R.string.empty_categories_list_viewing)
                                        ViewModelState.Editing -> stringResource(R.string.empty_categories_list_editing)
                                    }
                                }
                            },
                            icon = Icons.Rounded.DataArray,
                            iconModifier = Modifier.scale(2.5f),
                            modifier = Modifier
                                .padding(bottom = bottomPadding)
                                .align(Alignment.Center)
                                .fillMaxSize()
                        )
                    }

                    is ListState.Stable -> CategoriesList(
                        lazyListState = lazyListState,
                        categoriesList = listState.data,
                        viewModel = viewModel,
                        bottomPadding = with(LocalDensity.current) {
                            (bottomPadding + fabHeightPx.floatValue.toDp()).animate()
                        }
                    )
                }
            }
        }


        AnimatedVisibility(
            visible = viewModel.isCreatingCategory,
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
                viewModel.push(CategoriesAction.AddCategory(name))
            }
        }
    }
}



@Composable
private fun CategoriesList(
    lazyListState: LazyListState,
    categoriesList: List<BasicCategory>,
    viewModel: CategoriesViewModel,
    bottomPadding: Dp
) {
    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(viewModel.categories) { index, category ->
            CategoryComposable(
                category = category,
                isEditing = viewModel.isEditing.value,
        itemsIndexed(categoriesList) { index, category ->
                isEditing = viewModel.viewModelState == ViewModelState.Editing,
                isSwiped = viewModel.selectedCategoryIndex == index,
                onSwipe = { isSwiped ->
                    viewModel.push(
                        CategoriesAction.SwipeCategory(
                            position = index,
                            isOpened = isSwiped
                        )
                    )
                },
                onClick = {
                    viewModel.onItemClick {
                        viewModel.push(
                            CategoriesAction.SwipeCategory(isOpened = false)
                        )
                        viewModel.push(
                            CategoriesAction.NavigateToCategory(CategoryArgs(id = category.id))
                        )
                    }
                },
                onEdit = {
                    viewModel.onItemClick {
                        viewModel.push(
                            CategoriesAction.SwipeCategory(isOpened = false)
                        )
                        viewModel.push(
                            CategoriesAction.NavigateToCategory(
                                args = CategoryArgs(id = category.id),
                                isEditing = true
                            )
                        )
                    }
                },
                onDelete = {
                    viewModel.onItemClick {
                        viewModel.push(
                            CategoriesAction.SwipeCategory(isOpened = false)
                        )

                        viewModel.push(
                            CategoriesAction.OpenDialog(DialogType.ConfirmDeletion(category))
                        )
                    }
                }
            )
        }
        item {
            Spacer(modifier = Modifier.height(bottomPadding))
        }
    }
}


@Composable
private fun CategoryComposable(
    category: Category,
    isEditing: Boolean,
    isSwiped: Boolean,
    onSwipe: suspend (isSwiped: Boolean) -> Unit,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = rememberScrollableListItemState(isSwiped)
    val onClickState = rememberUpdatedState(newValue = onClick)

    LaunchedEffect(isSwiped) {
        if (isSwiped != state.isSwiped.value) {
            state.swipe()
        }
    }
    LaunchedEffect(key1 = state.isSwiped.value) {
        if (state.isSwiped.value != isSwiped) {
            onSwipe(state.isSwiped.value)
        }
    }

    ScrollableListItem(
        state = state,
        modifier = modifier,
        onClick = if (isEditing) null else onClickState.value,
        containerColor = MaterialTheme.colorScheme.background,
        hiddenContent = {
            EditDeleteContent(
                onEditClick = onEdit,
                onDeleteClick = onDelete
            )
        }
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                if (category.maxCashback == null) {
                    Text(
                        text = stringResource(R.string.no_cashbacks_for_category),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            trailingContent = {
                category.maxCashback?.let { BasicInfoCashback(cashback = it) }
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
                headlineColor = MaterialTheme.colorScheme.onBackground.animate(),
                supportingColor = MaterialTheme.colorScheme.error.animate(),
                trailingIconColor = MaterialTheme.colorScheme.primary.animate()
            )
        )
    }
}