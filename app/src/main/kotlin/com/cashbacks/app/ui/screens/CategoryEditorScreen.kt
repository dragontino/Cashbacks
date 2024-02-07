package com.cashbacks.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EditOff
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.cashbacks.app.R
import com.cashbacks.app.ui.composables.BasicFloatingActionButton
import com.cashbacks.app.ui.composables.CashbackComposable
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.ConfirmDeletionDialog
import com.cashbacks.app.ui.composables.ConfirmExitWithSaveDataDialog
import com.cashbacks.app.ui.composables.DisposableEffectWithLifecycle
import com.cashbacks.app.ui.composables.EditableTextField
import com.cashbacks.app.ui.composables.ModalSheetDefaults
import com.cashbacks.app.ui.composables.NewNameTextField
import com.cashbacks.app.ui.composables.SecondaryListContentTabPage
import com.cashbacks.app.ui.composables.SecondaryTabsLayout
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ScreenEvents
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.ui.screens.navigation.AppScreens
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.floatingActionButtonEnterAnimation
import com.cashbacks.app.util.floatingActionButtonExitAnimation
import com.cashbacks.app.util.keyboardAsState
import com.cashbacks.app.viewmodel.CategoryEditorViewModel
import com.cashbacks.domain.model.Cashback
import com.cashbacks.domain.model.Shop
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryEditorScreen(
    viewModel: CategoryEditorViewModel,
    navigateTo: (route: String) -> Unit,
    popBackStack: () -> Unit
) {
    BackHandler {
        when {
            viewModel.category.value.isChanged -> viewModel.openDialog(DialogType.Save)
            else -> viewModel.navigateTo(AppScreens.Categories.createUrl())
        }
    }

    DisposableEffectWithLifecycle(
        onDestroy = viewModel::save
    )

    val snackbarState = remember(::SnackbarHostState)
    val scope = rememberCoroutineScope()

    val showSnackbar = remember {
        fun(message: String) {
            scope.launch { snackbarState.showSnackbar(message) }
        }
    }


    val keyboardIsOpen = keyboardAsState()
    LaunchedEffect(Unit) {
        snapshotFlow { keyboardIsOpen.value }.collectLatest { isKeyboardOpen ->
            if (!isKeyboardOpen) {
                viewModel.addingShopState.value = false
            }
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


    when (dialogType) {
        is DialogType.ConfirmDeletion<*> -> {
            ConfirmDeletionDialog(
                text = stringResource(
                    R.string.confirm_category_deletion,
                    viewModel.category.value.name
                ),
                onConfirm = remember {
                    fun() {
                        viewModel.closeDialog()
                        viewModel.deleteCategory()
                    }
                },
                onDismiss = viewModel::closeDialog
            )
        }
        DialogType.Save -> {
            ConfirmExitWithSaveDataDialog(
                onConfirm = {
                    viewModel.save()
                    viewModel.navigateTo(null)
                },
                onDismiss = { viewModel.navigateTo(null) },
                onClose = viewModel::closeDialog
            )
        }
        null -> {}
    }

    Box(
        modifier = Modifier
            .imePadding()
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (viewModel.state.value) {
            ViewModelState.Loading -> LoadingInBox(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            )
            else -> CategoryInfoScreenContent(viewModel = viewModel, snackbarState)
        }

        AnimatedVisibility(
            visible = viewModel.state.value != ViewModelState.Loading && viewModel.addingShopState.value,
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



@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CategoryInfoScreenContent(
    viewModel: CategoryEditorViewModel,
    snackbarState: SnackbarHostState
) {
    val tabPages = arrayOf(
        AppScreens.Shop,
        AppScreens.Cashback
    )
    val pagerState = rememberPagerState { tabPages.size }
    val currentScreen = remember {
        derivedStateOf { tabPages[pagerState.currentPage] }
    }

    CollapsingToolbarScaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = AppScreens.CategoryEditor.title(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    Crossfade(
                        targetState = viewModel.category.value.isChanged,
                        label = "icon animation",
                        animationSpec = tween(durationMillis = 200, easing = LinearEasing)
                    ) { isChanged ->
                        IconButton(
                            onClick = {
                                viewModel.onItemClick {
                                    if (isChanged) viewModel.openDialog(DialogType.Save)
                                    else viewModel.navigateTo(AppScreens.Categories.createUrl())
                                }
                            }
                        ) {
                            Icon(
                                imageVector = when {
                                    isChanged -> Icons.Rounded.Close
                                    else -> Icons.Rounded.ArrowBackIosNew
                                },
                                contentDescription = "return to previous screen",
                                modifier = Modifier.scale(1.2f)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.onItemClick {
                                viewModel.openDialog(DialogType.ConfirmDeletion(viewModel.category))
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "delete category",
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
            AnimatedVisibility(
                visible = currentScreen.value == AppScreens.Shop && !viewModel.addingShopState.value,
                enter = floatingActionButtonEnterAnimation(),
                exit = floatingActionButtonExitAnimation()
            ) {
                BasicFloatingActionButton(icon = Icons.Rounded.Add) {
                    viewModel.onItemClick {
                        viewModel.addingShopState.value = true
                    }
                }
            }

            Crossfade(
                targetState = currentScreen.value,
                label = "fab anim",
                animationSpec = tween(durationMillis = 200, easing = LinearEasing)
            ) { currentScreen ->
                when (currentScreen) {
                    AppScreens.Cashback -> CashbackFAB(expanded = !pagerState.isScrollInProgress) {
                        viewModel.navigateTo(
                            AppScreens.Cashback.createUrlFromCategory(
                                id = null,
                                categoryId = viewModel.categoryId,
                            ),
                        )
                    }
                    AppScreens.Shop -> {
                        BasicFloatingActionButton(icon = Icons.Rounded.EditOff) {
                            viewModel.onItemClick {
                                viewModel.save()
                                viewModel.navigateTo(
                                    AppScreens.CategoryViewer.createUrl(viewModel.categoryId)
                                )
                            }
                        }
                    }
                }
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
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface.animate())
                .padding(contentPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EditableTextField(
                text = viewModel.category.value.name,
                onTextChange = viewModel.category.value::name::set,
                label = stringResource(R.string.category_placeholder),
                imeAction = ImeAction.Done,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )

            SecondaryTabsLayout(
                pages = tabPages,
                scrollEnabled = !viewModel.addingShopState.value,
                modifier = Modifier
                    .shadow(elevation = 20.dp, shape = ModalSheetDefaults.BottomSheetShape)
                    .background(MaterialTheme.colorScheme.background.animate())
                    .padding(top = 8.dp)
                    .clip(ModalSheetDefaults.BottomSheetShape)
            ) { page ->
                SecondaryListContentTabPage(
                    items = when (page) {
                        AppScreens.Cashback -> viewModel.cashbacksLiveData.observeAsState().value
                        AppScreens.Shop -> viewModel.shopsLiveData.observeAsState().value
                    },
                    placeholderText = when (page) {
                        AppScreens.Cashback -> stringResource(R.string.empty_cashbacks_list)
                        AppScreens.Shop -> stringResource(R.string.empty_shops_list_editing)
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
                            isInEdit = true
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
}




@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.ShopFABs(
    onAdd: () -> Unit,
    onSave: () -> Unit
) {
    BasicFloatingActionButton(icon = Icons.Rounded.Add, onClick = onAdd)
    BasicFloatingActionButton(icon = Icons.Rounded.EditOff, onClick = onSave)
}


@Composable
fun CashbackFAB(expanded: Boolean, onAdd: () -> Unit) {
    ExtendedFloatingActionButton(
        text = {
            Text(
                text = stringResource(R.string.add_cashback),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        icon = {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "add cashback"
            )
        },
        expanded = expanded,
        onClick = onAdd,
        containerColor = MaterialTheme.colorScheme.primaryContainer.animate(),
        contentColor = MaterialTheme.colorScheme.onPrimary.animate()
    )
}
