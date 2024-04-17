package com.cashbacks.app.ui.features.home.categories

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import com.cashbacks.domain.R
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
import com.cashbacks.domain.model.Category
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    title: String,
    openDrawer: () -> Unit,
    navigateToCategory: (args: CategoryArgs) -> Unit,
    popBackStack: () -> Unit,
    bottomPadding: Dp,
    modifier: Modifier = Modifier
) {
    BackHandler {
        when {
            viewModel.isEditing.value -> viewModel.isEditing.value = false
            else -> popBackStack()
        }
    }

    val snackbarHostState = remember(::SnackbarHostState)
    val scope = rememberCoroutineScope()

    val lazyListState = rememberLazyListState()
    val keyboardState = keyboardAsState()
    var dialogType: DialogType? by rememberSaveable { mutableStateOf(null) }

    val showSnackbar = remember {
        fun(message: String) {
            scope.launch {
                if (message.isNotBlank()) snackbarHostState.showSnackbar(message)
            }
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { keyboardState.value }.collect { isKeyboardOpen ->
            if (!isKeyboardOpen) {
                viewModel.addingCategoriesState = false
            }
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventsFlow.collect { event ->
            when (event) {
                is ScreenEvents.OpenDialog -> dialogType = event.type
                ScreenEvents.CloseDialog -> dialogType = null
                is ScreenEvents.Navigate -> (event.args as? CategoryArgs)
                    ?.let(navigateToCategory)
                    ?: popBackStack()
                is ScreenEvents.ShowSnackbar -> event.message.let(showSnackbar)
            }
        }
    }
    dialogType.takeIf { it is DialogType.ConfirmDeletion<*> }?.let { type ->
        val category = (type as DialogType.ConfirmDeletion<*>).value as Category
        ConfirmDeletionDialog(
            text = stringResource(
                R.string.confirm_category_deletion,
                category.name
            ),
            onConfirm = { viewModel.deleteCategory(category) },
            onClose = viewModel::closeDialog
        )
    }

    val fabOffsetPx = rememberSaveable { mutableFloatStateOf(0f) }


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
                    visible = viewModel.isEditing.value
                            && viewModel.appBarState != HomeTopAppBarState.Search
                            && !viewModel.addingCategoriesState,
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
                    BasicFloatingActionButton(
                        icon = when {
                            viewModel.isEditing.value -> Icons.Rounded.EditOff
                            else -> Icons.Rounded.Edit
                        },
                        onClick = remember {
                            fun() {
                                viewModel.onItemClick {
                                    viewModel.isEditing.value = !viewModel.isEditing.value
                                }
                            }
                        }
                    )
                }
            },
            contentWindowInsets = WindowInsets.ime.only(WindowInsetsSides.Bottom),
            fabModifier = Modifier
                .onGloballyPositioned { fabOffsetPx.floatValue = it.size.height.toFloat() }
                .windowInsetsPadding(WindowInsets.tappableElement.only(WindowInsetsSides.End))
                .padding(bottom = bottomPadding),
            modifier = modifier.fillMaxSize()
        ) {
            when (viewModel.state.value) {
                ListState.Loading -> LoadingInBox(
                    modifier = Modifier.padding(bottom = bottomPadding)
                )

                ListState.Empty -> EmptyList(
                    text = when {
                        !viewModel.isEditing.value -> stringResource(R.string.empty_categories_list_viewing)
                        viewModel.isSearch -> {
                            when {
                                viewModel.query.value.isBlank() -> stringResource(R.string.empty_search_query)
                                else -> stringResource(R.string.empty_search_results)
                            }
                        }

                        else -> stringResource(R.string.empty_categories_list_editing)
                    },
                    icon = Icons.Rounded.DataArray,
                    iconModifier = Modifier.scale(2.5f),
                    modifier = Modifier
                        .padding(bottom = bottomPadding)
                        .align(Alignment.Center)
                        .fillMaxSize()
                )

                ListState.Stable -> CategoriesList(
                    lazyListState = lazyListState,
                    viewModel = viewModel,
                    bottomPadding = with(LocalDensity.current) {
                        (bottomPadding + fabOffsetPx.floatValue.toDp()).animate()
                    }
                )
            }
        }


        AnimatedVisibility(
            visible = viewModel.addingCategoriesState,
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
            NewNameTextField(
                placeholder = stringResource(R.string.category_placeholder)
            ) { name ->
                viewModel.addCategory(name)
                viewModel.addingCategoriesState = false
            }
        }
    }
}



@Composable
private fun CategoriesList(
    lazyListState: LazyListState,
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
                isSwiped = viewModel.selectedCategoryIndex == index,
                onSwipe = { isSwiped ->
                    viewModel.selectedCategoryIndex = when {
                        isSwiped -> index
                        else -> null
                    }
                },
                onClick = {
                    viewModel.onItemClick {
                        viewModel.navigateTo(CategoryArgs(id = category.id, isEditing = false))
                    }
                },
                onEdit = {
                    viewModel.selectedCategoryIndex = -1
                    viewModel.navigateTo(CategoryArgs(id = category.id, isEditing = true))
                },
                onDelete = remember {
                    fun() {
                        viewModel.onItemClick {
                            viewModel.selectedCategoryIndex = -1
                            viewModel.openDialog(DialogType.ConfirmDeletion(category))
                        }
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