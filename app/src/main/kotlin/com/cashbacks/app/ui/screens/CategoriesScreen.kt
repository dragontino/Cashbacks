package com.cashbacks.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DataArray
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cashbacks.app.R
import com.cashbacks.app.ui.composables.BasicInfoCashback
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.EditDeleteContent
import com.cashbacks.app.ui.composables.EmptyList
import com.cashbacks.app.ui.composables.NewNameTextField
import com.cashbacks.app.ui.composables.ScrollableListItem
import com.cashbacks.app.ui.managment.ListState
import com.cashbacks.app.ui.screens.navigation.AppScreens
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.keyboardAsState
import com.cashbacks.app.util.smoothScrollToItem
import com.cashbacks.app.viewmodel.CategoriesViewModel
import com.cashbacks.domain.model.Category
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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


    Crossfade(
        targetState = viewModel.state.value,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "categories_list_animation",
        modifier = Modifier
            .imePadding()
            .fillMaxSize()
    ) { state ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            when (state) {
                ListState.Loading -> LoadingInBox(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                )

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

                ListState.Stable -> CategoriesScreen(
                    viewModel = viewModel,
                    openDrawer = openDrawer,
                    navigateTo = navigateTo,
                    modifier = Modifier.fillMaxSize()
                )
            }

            AnimatedVisibility(
                visible = state != ListState.Loading && viewModel.addingCategoriesState.value,
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



@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    openDrawer: () -> Unit,
    navigateTo: (route: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember(::SnackbarHostState)
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    fun showSnackbar(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(message = message)
        }
    }

    val keyboardIsOpen = keyboardAsState()
    LaunchedEffect(keyboardIsOpen.value) {
        if (!keyboardIsOpen.value) {
            viewModel.addingCategoriesState.value = false
        }
    }


    CollapsingToolbarScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(AppScreens.Categories.titleRes),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    OutlinedIconButton(
                        onClick = {
                            viewModel.onItemClick(openDrawer)
                        },
                        shape = CircleShape,
                        border = BorderStroke(
                            width = 1.8.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        )
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
        floatingActionButton = {
            AnimatedVisibility(
                visible = !viewModel.addingCategoriesState.value,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    text = {
                        if (viewModel.isEditing.value) {
                            Text(
                                text = stringResource(R.string.add_category),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = when {
                                viewModel.isEditing.value -> Icons.Rounded.Add
                                else -> Icons.Rounded.Edit
                            },
                            contentDescription = "add category"
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer.animate(),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer.animate(),
                    onClick = {
                        viewModel.onItemClick {
                            if (viewModel.isEditing.value) {
                                viewModel.addingCategoriesState.value = true
                                scope.launch {
                                    delay(700)
                                    lazyListState.smoothScrollToItem(viewModel.categories.value.lastIndex)
                                }
                            } else {
                                viewModel.isEditing.value = true
                            }
                        }
                    },
                    expanded = viewModel.isEditing.value,
                    elevation = FloatingActionButtonDefaults.loweredElevation(),
                    modifier = Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary.animate(),
                        shape = FloatingActionButtonDefaults.extendedFabShape
                    )
                )
            }
        },
        fabPosition = if (viewModel.isEditing.value) FabPosition.Center else FabPosition.End,
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
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.padding(contentPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(viewModel.categories.value) { index, category ->
                CategoryComposable(
                    category = category,
                    isEditing = viewModel.isEditing.value,
                    isSwiped = viewModel.swipedItemIndex.intValue == index,
                    onClick = {
                        viewModel.onItemClick {
                            viewModel.swipedItemIndex.intValue = -1
                            navigateTo(AppScreens.Category.createUrl(category.id))
                        }
                    },
                    onEdit = {
                        viewModel.onItemClick {
                            viewModel.swipedItemIndex.intValue = -1
                            navigateTo(AppScreens.Category.createUrl(category.id, isEdit = true))
                        }
                    },
                    onDelete = { viewModel.deleteCategory(category, ::showSnackbar) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }
}



@Composable
private fun CategoryComposable(
    category: Category,
    isEditing: Boolean,
    isSwiped: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ScrollableListItem(
        onClick = if (isEditing) null else onClick,
        hiddenContent = {
            EditDeleteContent(
                onEditClick = onEdit,
                onDeleteClick = onDelete
            )
        },
        initialAlignment = when {
            isSwiped -> Alignment.Start
            else -> Alignment.End
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