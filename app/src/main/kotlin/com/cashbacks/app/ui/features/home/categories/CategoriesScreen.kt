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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cashbacks.app.ui.composables.BasicFloatingActionButton
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.ConfirmDeletionDialog
import com.cashbacks.app.ui.composables.EmptyList
import com.cashbacks.app.ui.composables.MaxCashbackOwnerComposable
import com.cashbacks.app.ui.composables.NewNameTextField
import com.cashbacks.app.ui.features.category.CategoryArgs
import com.cashbacks.app.ui.features.home.HomeAppBarDefaults
import com.cashbacks.app.ui.features.home.HomeTopAppBar
import com.cashbacks.app.ui.features.home.HomeTopAppBarState
import com.cashbacks.app.ui.features.home.categories.mvi.CategoriesAction
import com.cashbacks.app.ui.features.home.categories.mvi.CategoriesEvent
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ListState
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.util.AnimationDefaults
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.floatingActionButtonEnterAnimation
import com.cashbacks.app.util.floatingActionButtonExitAnimation
import com.cashbacks.app.util.keyboardAsState
import com.cashbacks.app.util.mix
import com.cashbacks.app.util.reversed
import com.cashbacks.app.util.smoothScrollToItem
import com.cashbacks.domain.R
import com.cashbacks.domain.model.Category
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    title: String,
    contentState: LazyListState,
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
    val topBarState = rememberTopAppBarState()
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
                    contentState.smoothScrollToItem(contentState.layoutInfo.totalItemsCount)
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
            topBar = {
                HomeTopAppBar(
                    title = title,
                    state = viewModel.appBarState,
                    onStateChange = {
                        viewModel.push(CategoriesAction.UpdateAppBarState(it))
                    },
                    searchPlaceholder = stringResource(R.string.search_categories_placeholder),
                    onNavigationIconClick = openDrawer,
                    colors = HomeAppBarDefaults.colors(
                        topBarContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = .6f)
                            .mix(MaterialTheme.colorScheme.primary)
                            .ratio(topBarState.overlappedFraction),
                    ),
                )
            },
            topBarState = topBarState,
            contentState = contentState,
            topBarScrollEnabled = viewModel.appBarState is HomeTopAppBarState.TopBar,
            snackbarHost = {
                SnackbarHost(snackbarHostState) {
                    Snackbar(
                        snackbarData = it,
                        containerColor = MaterialTheme.colorScheme.background.reversed.animate(),
                        contentColor = MaterialTheme.colorScheme.onBackground.reversed.animate(),
                        shape = MaterialTheme.shapes.medium,
                    )
                }
            },
            floatingActionButtons = {
                AnimatedVisibility(
                    visible = viewModel.viewModelState == ViewModelState.Editing
                            && viewModel.appBarState !is HomeTopAppBarState.Search
                            && !viewModel.isCreatingCategory,
                    enter = floatingActionButtonEnterAnimation(),
                    exit = floatingActionButtonExitAnimation(),
                ) {
                    BasicFloatingActionButton(icon = Icons.Rounded.Add) {
                        viewModel.onItemClick {
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
                        },
                    )
                }
            },
            contentWindowInsets = WindowInsets.ime.only(WindowInsetsSides.Bottom),
            fabModifier = Modifier
                .onGloballyPositioned { fabHeightPx.floatValue = it.size.height.toFloat() }
                .windowInsetsPadding(WindowInsets.tappableElement.only(WindowInsetsSides.End))
                .padding(bottom = bottomPadding),
            modifier = modifier.fillMaxSize(),
        ) {
            CategoriesList(
                lazyListState = contentState,
                viewModel = viewModel,
                bottomPadding = with(LocalDensity.current) {
                    (bottomPadding + fabHeightPx.floatValue.toDp())
                },
                modifier = Modifier.align(Alignment.Center)
            )
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
    viewModel: CategoriesViewModel,
    bottomPadding: Dp,
    modifier: Modifier = Modifier
) {
    val categoriesState = viewModel.categoriesFlow.collectAsStateWithLifecycle()
    Crossfade(
        targetState = categoriesState.value,
        label = "Content loading animation",
        animationSpec = AnimationDefaults.loadingContentAnimationSpec()
    ) { categoriesList ->
        when (val listState = ListState.fromList(categoriesList)) {
            is ListState.Loading -> {
                LoadingInBox(
                    modifier = Modifier.padding(bottom = bottomPadding)
                )
            }

            is ListState.Empty -> {
                Crossfade(
                    targetState = viewModel.appBarState to viewModel.viewModelState,
                    label = "emptyList",
                    animationSpec = AnimationDefaults.loadingContentAnimationSpec()
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
                            .then(modifier)
                            .padding(bottom = bottomPadding)
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
                    itemsIndexed(listState.data) { index, category ->
                        MaxCashbackOwnerComposable(
                            cashbackOwner = category,
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
        }
    }
}