package com.cashbacks.app.ui.screens

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DataArray
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cashbacks.app.R
import com.cashbacks.app.ui.composables.BasicInfoAboutCashback
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.NewNameTextField
import com.cashbacks.app.ui.composables.ScrollableListItem
import com.cashbacks.app.ui.screens.navigation.AppScreens
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.keyboardAsState
import com.cashbacks.app.util.smoothScrollToItem
import com.cashbacks.app.viewmodel.CategoriesViewModel
import com.cashbacks.app.viewmodel.CategoriesViewModel.ViewModelState
import com.cashbacks.domain.model.Category
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    openDrawer: () -> Unit,
    navigateTo: (route: String) -> Unit
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
        modifier = Modifier.fillMaxSize(),
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
                        Text(
                            text = stringResource(R.string.add_category),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "add category"
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer.animate(),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer.animate(),
                    onClick = {
                        viewModel.onItemClick {
                            viewModel.addingCategoriesState.value = true
                            scope.launch {
                                delay(700)
                                lazyListState.smoothScrollToItem(viewModel.categories.value.lastIndex)
                            }
                        }
                    },
                    elevation = FloatingActionButtonDefaults.loweredElevation(),
                    modifier = Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary.animate(),
                        shape = FloatingActionButtonDefaults.extendedFabShape
                    )
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
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "categories_list_animation",
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) { state ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                when (state) {
                    ViewModelState.Loading -> LoadingInBox(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    )

                    ViewModelState.EmptyList -> EmptyList(
                        text = stringResource(R.string.empty_categories_list),
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.DataArray,
                                contentDescription = "empty list",
                                tint = MaterialTheme.colorScheme.onBackground.animate(),
                                modifier = Modifier.scale(2.5f)
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxSize()
                    )

                    ViewModelState.Ready -> CategoriesScreen(
                        viewModel = viewModel,
                        listState = lazyListState,
                        onClick = { category, isEdit ->
                            viewModel.onItemClick {
                                viewModel.swipedItemIndex.intValue = -1
                                navigateTo(
                                    AppScreens.Category.createUrl(
                                        id = category.id,
                                        isEdit = isEdit,
                                    ),
                                )
                            }
                        },
                        showSnackbar = ::showSnackbar,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                AnimatedVisibility(
                    visible = state != ViewModelState.Loading && viewModel.addingCategoriesState.value,
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
private fun EmptyList(
    text: String,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit) = {}
) {
    Box(
        modifier = Modifier
            .then(modifier)
            .verticalScroll(rememberScrollState()),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(vertical = 20.dp)
                .align(Alignment.Center)
                .matchParentSize()
        ) {
            icon()

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.animate(),
                textAlign = TextAlign.Center
            )
        }
    }
}



@Composable
private fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    onClick: (category: Category, isEdit: Boolean) -> Unit,
    showSnackbar: (message: String) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(viewModel.categories.value) { index, category ->
            ScrollableListItem(
                onClick = { onClick(category, false) },
                hiddenContent = {
                    IconButton(
                        onClick = { onClick(category, true) },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary.animate()
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "edit",
                            modifier = Modifier.scale(1.1f)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.deleteCategory(category, showSnackbar) },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary.animate()
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "delete",
                            modifier = Modifier.scale(1.1f)
                        )
                    }
                },
                isSwiped = remember {
                    derivedStateOf { viewModel.swipedItemIndex.intValue == index }
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
                        category.maxCashback?.let { BasicInfoAboutCashback(cashback = it) }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.background.animate(),
                        supportingColor = MaterialTheme.colorScheme.error.animate(),
                        trailingIconColor = MaterialTheme.colorScheme.primary.animate()
                    )
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}