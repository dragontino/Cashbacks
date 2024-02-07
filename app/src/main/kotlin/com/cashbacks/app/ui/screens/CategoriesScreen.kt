package com.cashbacks.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DataArray
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditOff
import androidx.compose.material.icons.rounded.Menu
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cashbacks.app.R
import com.cashbacks.app.ui.composables.BasicFloatingActionButton
import com.cashbacks.app.ui.composables.BasicInfoCashback
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.ConfirmDeletionDialog
import com.cashbacks.app.ui.composables.EditDeleteContent
import com.cashbacks.app.ui.composables.EmptyList
import com.cashbacks.app.ui.composables.NewNameTextField
import com.cashbacks.app.ui.composables.ScrollableListItem
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ListState
import com.cashbacks.app.ui.managment.ScreenEvents
import com.cashbacks.app.ui.managment.rememberScrollableListItemState
import com.cashbacks.app.ui.screens.navigation.AppScreens
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.floatingActionButtonEnterAnimation
import com.cashbacks.app.util.floatingActionButtonExitAnimation
import com.cashbacks.app.util.keyboardAsState
import com.cashbacks.app.util.smoothScrollToItem
import com.cashbacks.app.viewmodel.CategoriesViewModel
import com.cashbacks.domain.model.Category
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    openDrawer: () -> Unit,
    navigateTo: (route: String) -> Unit,
    popBackStack: () -> Unit
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
                viewModel.addingCategoriesState.value = false
            }
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventsFlow.collect { event ->
            when (event) {
                is ScreenEvents.OpenDialog -> dialogType = event.type
                ScreenEvents.CloseDialog -> dialogType = null
                is ScreenEvents.Navigate -> event.route?.let(block = navigateTo)
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
            onConfirm = {
                viewModel.deleteCategory(category)
                viewModel.closeDialog()
            },
            onDismiss = viewModel::closeDialog
        )
    }

    CollapsingToolbarScaffold(
        modifier = Modifier.imePadding().fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = AppScreens.Categories.title(),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = remember {
                            fun() {
                                viewModel.onItemClick(onClick = openDrawer)
                            }
                        }
                    ) {
                        Icon(Icons.Rounded.Menu, contentDescription = "open menu")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary.animate(),
                    titleContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary.animate()
                )
            )
        },
        floatingActionButtons = {
            AnimatedVisibility(
                visible = viewModel.isEditing.value && !viewModel.addingCategoriesState.value,
                enter = floatingActionButtonEnterAnimation(),
                exit = floatingActionButtonExitAnimation()
            ) {
                BasicFloatingActionButton(icon = Icons.Rounded.Add) {
                    viewModel.onItemClick {
                        viewModel.addingCategoriesState.value = true
                        scope.launch {
                            delay(700)
                            lazyListState.smoothScrollToItem(viewModel.categories.value.lastIndex)
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = !viewModel.addingCategoriesState.value,
            ) {
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
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    shape = MaterialTheme.shapes.medium,
                    containerColor = MaterialTheme.colorScheme.onBackground.animate(),
                    contentColor = MaterialTheme.colorScheme.background.animate()
                )
            }
        }
    ) { contentPadding ->
        Crossfade(
            targetState = viewModel.state.value,
            animationSpec = tween(durationMillis = 100, easing = LinearEasing),
            label = "loading animation",
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) { state ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                when (state) {
                    ListState.Loading -> LoadingInBox()
                    ListState.Empty -> EmptyList(
                        text = when {
                            viewModel.isEditing.value -> stringResource(R.string.empty_categories_list_editing)
                            else -> stringResource(R.string.empty_categories_list_viewing)
                        },
                        icon = Icons.Rounded.DataArray,
                        iconModifier = Modifier.scale(2.5f),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxSize()
                    )

                    else -> CategoriesList(
                        lazyListState = lazyListState,
                        viewModel = viewModel
                    )
                }

                AnimatedVisibility(
                    visible = viewModel.addingCategoriesState.value,
                    enter = expandVertically(
                        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                    ),
                    exit = shrinkVertically(
                        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                    ),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    NewNameTextField(
                        placeholder = stringResource(R.string.category_placeholder)
                    ) { name ->
                        viewModel.addCategory(name)
                        viewModel.addingCategoriesState.value = false
                    }
                }
            }
        }
    }
}



@Composable
private fun CategoriesList(
    lazyListState: LazyListState,
    viewModel: CategoriesViewModel
) {
    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(viewModel.categories.value) { index, category ->
            CategoryComposable(
                category = category,
                isSwiped = viewModel.selectedCategoryIndex == index,
                onSwipe = { isSwiped ->
                    viewModel.selectedCategoryIndex = when {
                        isSwiped -> index
                        else -> null
                    }
                },
                onClick = when {
                    viewModel.isEditing.value -> null
                    else -> fun() {
                        viewModel.onItemClick {
                            viewModel.navigateTo(AppScreens.CategoryViewer.createUrl(category.id))
                        }
                    }
                },
                onEdit = {
                    viewModel.selectedCategoryIndex = -1
                    viewModel.navigateTo(AppScreens.CategoryEditor.createUrl(category.id))
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
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}


@Composable
private fun CategoryComposable(
    category: Category,
    isSwiped: Boolean,
    onSwipe: suspend (isSwiped: Boolean) -> Unit,
    onClick: (() -> Unit)?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = rememberScrollableListItemState(isSwiped)
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
        onClick = onClick,
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
                    style = MaterialTheme.typography.bodyMedium
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
                containerColor = MaterialTheme.colorScheme.background.animate(),
                supportingColor = MaterialTheme.colorScheme.error.animate(),
                trailingIconColor = MaterialTheme.colorScheme.primary.animate()
            )
        )
    }
}