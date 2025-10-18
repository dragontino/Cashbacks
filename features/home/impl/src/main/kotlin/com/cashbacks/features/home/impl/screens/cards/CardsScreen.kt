package com.cashbacks.features.home.impl.screens.cards

import android.content.ClipData
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cashbacks.common.composables.BasicFloatingActionButton
import com.cashbacks.common.composables.BoundedSnackbar
import com.cashbacks.common.composables.CollapsingToolbarScaffold
import com.cashbacks.common.composables.ConfirmDeletionDialog
import com.cashbacks.common.composables.EmptyList
import com.cashbacks.common.composables.LoadingInBox
import com.cashbacks.common.composables.management.DialogType
import com.cashbacks.common.composables.management.ListState
import com.cashbacks.common.composables.management.toListState
import com.cashbacks.common.composables.swipeable.SwipeableListItem
import com.cashbacks.common.composables.swipeable.SwipeableListItemDefaults
import com.cashbacks.common.composables.swipeable.rememberSwipeableItemState
import com.cashbacks.common.composables.theme.CashbacksTheme
import com.cashbacks.common.composables.theme.VerdanaFont
import com.cashbacks.common.composables.utils.animate
import com.cashbacks.common.composables.utils.expandedAnimationSpec
import com.cashbacks.common.composables.utils.mix
import com.cashbacks.common.composables.utils.reversed
import com.cashbacks.common.resources.R
import com.cashbacks.common.utils.OnClick
import com.cashbacks.common.utils.mvi.IntentSender
import com.cashbacks.features.bankcard.domain.model.BankCard
import com.cashbacks.features.bankcard.domain.model.BasicBankCard
import com.cashbacks.features.bankcard.domain.model.PrimaryBankCard
import com.cashbacks.features.bankcard.domain.utils.BankCardUtils.getHiddenNumber
import com.cashbacks.features.bankcard.domain.utils.BankCardUtils.withSpaces
import com.cashbacks.features.bankcard.presentation.api.BankCardArgs
import com.cashbacks.features.bankcard.presentation.api.composables.PlasticBankCard
import com.cashbacks.features.bankcard.presentation.api.utils.BankCardPresentationUtils.getDisplayableString
import com.cashbacks.features.bankcard.presentation.api.utils.PaymentSystemUtils
import com.cashbacks.features.home.impl.composables.HomeAppBarDefaults
import com.cashbacks.features.home.impl.composables.HomeTopAppBar
import com.cashbacks.features.home.impl.composables.HomeTopAppBarState
import com.cashbacks.features.home.impl.mvi.BankCardsIntent
import com.cashbacks.features.home.impl.mvi.BankCardsLabel
import com.cashbacks.features.home.impl.mvi.BankCardsState
import com.cashbacks.features.home.impl.navigation.HomeDestination
import com.cashbacks.features.home.impl.utils.LocalBottomBarHeight
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun BankCardsRoot(
    openDrawer: () -> Unit,
    navigateToCard: (args: BankCardArgs) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CardsViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember(::SnackbarHostState)
    var dialogType: DialogType? by rememberSaveable { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        viewModel.labelFlow.collect { label ->
            when (label) {
                is BankCardsLabel.NavigateToBankCard -> navigateToCard(label.args)
                is BankCardsLabel.DisplayMessage -> launch {
                    snackbarHostState.showSnackbar(label.message)
                }
                is BankCardsLabel.OpenNavigationDrawer -> openDrawer()
                is BankCardsLabel.ChangeOpenedDialog -> dialogType = label.type
            }
        }
    }

    when (val type = dialogType) {
        is DialogType.ConfirmDeletion<*> -> {
            val card = type.value as BasicBankCard
            ConfirmDeletionDialog(
                text = stringResource(
                    R.string.confirm_card_deletion,
                    card.name.ifBlank { card.getDisplayableString() }
                ),
                onConfirm = { viewModel.sendIntent(BankCardsIntent.DeleteBankCard(card)) },
                onClose = { viewModel.sendIntent(BankCardsIntent.CloseDialog) }
            )
        }
    }


    CardsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        intentSender = IntentSender(viewModel::sendIntent),
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardsScreen(
    state: BankCardsState,
    snackbarHostState: SnackbarHostState,
    intentSender: IntentSender<BankCardsIntent>,
    modifier: Modifier = Modifier
) {
    val topBarState = rememberTopAppBarState()
    val lazyListState = rememberLazyListState()
    val fabHeightPx = rememberSaveable { mutableFloatStateOf(0f) }

    CollapsingToolbarScaffold(
        topBar = {
            HomeTopAppBar(
                title = HomeDestination.Cards.screenTitle,
                state = state.appBarState,
                onStateChange = {
                    intentSender.send(BankCardsIntent.ChangeAppBarState(it))
                },
                searchPlaceholder = stringResource(R.string.search_cards_placeholder),
                onNavigationIconClick = {
                    intentSender.sendWithDelay(BankCardsIntent.ClickNavigationButton)
                },
                colors = HomeAppBarDefaults.colors(
                    topBarContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = .6f)
                        .mix(MaterialTheme.colorScheme.primary)
                        .ratio(topBarState.overlappedFraction)
                )
            )
        },
        topBarState = topBarState,
        contentState = lazyListState,
        topBarScrollEnabled = state.appBarState is HomeTopAppBarState.TopBar,
        floatingActionButtons = {
            AnimatedVisibility(visible = state.appBarState is HomeTopAppBarState.TopBar) {
                BasicFloatingActionButton(icon = Icons.Rounded.Add) {
                    intentSender.sendWithDelay(BankCardsIntent.CreateBankCard)
                }
            }
        },
        fabModifier = Modifier
            .padding(bottom = LocalBottomBarHeight.current)
            .windowInsetsPadding(WindowInsets.tappableElement.only(WindowInsetsSides.End))
            .onGloballyPositioned { fabHeightPx.floatValue = it.size.height.toFloat() },
        snackbarHost = {
            SnackbarHost(snackbarHostState) {
                BoundedSnackbar(
                    snackbarData = it,
                    containerColor = MaterialTheme.colorScheme.background.reversed.animate(),
                    contentColor = MaterialTheme.colorScheme.onBackground.reversed.animate(),
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
    ) {
        BankCardList(
            state = state,
            contentState = lazyListState,
            contentPadding = PaddingValues(
                bottom = with(LocalDensity.current) {
                    LocalBottomBarHeight.current + fabHeightPx.floatValue.toDp()
                }
            ),
            intentSender = intentSender,
        )
    }
}


@Composable
private fun BankCardList(
    state: BankCardsState,
    contentState: LazyListState,
    contentPadding: PaddingValues,
    intentSender: IntentSender<BankCardsIntent>,
    modifier: Modifier = Modifier
) {
    Crossfade(
        targetState = state.cards.toListState(),
        label = "loading animation",
        animationSpec = tween(durationMillis = 150, easing = LinearEasing),
        modifier = modifier.fillMaxSize(),
    ) { listState ->
        val context = LocalContext.current

        when (listState) {
            is ListState.Loading -> {
                LoadingInBox(Modifier.padding(contentPadding))
            }

            ListState.Empty -> {
                EmptyList(
                    text = when (state.appBarState) {
                        is HomeTopAppBarState.Search -> {
                            if (state.appBarState.query.isBlank()) {
                                stringResource(R.string.empty_search_query)
                            } else {
                                stringResource(
                                    R.string.empty_search_results,
                                    state.appBarState.query
                                )
                            }
                        }

                        is HomeTopAppBarState.TopBar -> stringResource(R.string.empty_bank_cards_list)
                    },
                    modifier = Modifier.padding(contentPadding)
                )
            }

            is ListState.Stable -> {
                LazyColumn(
                    state = contentState,
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    contentPadding.calculateTopPadding().takeIf { it.value > 0 }?.let {
                        item {
                            Spacer(Modifier.height(it))
                        }
                    }

                    items(
                        items = listState.data,
                        key = { it.id }
                    ) { card ->
                        BankCardListElement(
                            bankCard = card,
                            isExpanded = state.expandedCardId == card.id,
                            onExpandedStatusChanged = { isExpanded ->
                                intentSender.sendWithDelay(
                                    BankCardsIntent.ExpandCard(card.id, isExpanded)
                                )
                            },
                            isEnabledToSwipe = state.swipedCardId in setOf(card.id, null),
                            onSwipeStatusChanged = { isOnSwipe ->
                                intentSender.sendWithDelay(
                                    BankCardsIntent.SwipeCard(card.id, isOnSwipe)
                                )
                            },
                            onEdit = {
                                intentSender.sendWithDelay(BankCardsIntent.EditBankCard(card.id))
                            },
                            onDelete = {
                                intentSender.sendWithDelay(
                                    BankCardsIntent.OpenDialog(DialogType.ConfirmDeletion(card))
                                )
                            },
                            onOpenCardDetails = {
                                intentSender.sendWithDelay(BankCardsIntent.OpenBankCardDetails(card.id))
                            },
                            displayMessage = {
                                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                intentSender.sendWithDelay(BankCardsIntent.DisplayMessage(it))
                            },
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(
                                    start = contentPadding
                                        .calculateStartPadding(LocalLayoutDirection.current),
                                    end = contentPadding
                                        .calculateEndPadding(LocalLayoutDirection.current)
                                )
                        )
                    }

                    contentPadding.calculateBottomPadding().takeIf { it.value > 0 }?.let {
                        item {
                            Spacer(Modifier.height(it))
                        }
                    }
                }
            }
        }
    }
}


@Stable
@Composable
private fun BankCardListElement(
    bankCard: BankCard,
    modifier: Modifier = Modifier,
    isEnabledToSwipe: Boolean = true,
    isExpanded: Boolean = false,
    onSwipeStatusChanged: (isOnSwipe: Boolean) -> Unit = {},
    onExpandedStatusChanged: (isExpanded: Boolean) -> Unit = {},
    onEdit: OnClick = {},
    onDelete: OnClick = {},
    onOpenCardDetails: OnClick = {},
    displayMessage: (String) -> Unit = {},
) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val listItemState = rememberSwipeableItemState(
        leftAction = { onEdit() },
        rightAction = {
            onDelete()
            swipeToLeft()
        }
    )
    val scope = rememberCoroutineScope()
    val expandAnimation = remember { Animatable(initialValue = 0f) }
    val arrowDegrees = remember {
        derivedStateOf { expandAnimation.value * 90 }
    }
    val elevation = remember {
        derivedStateOf {
            (4.9 * expandAnimation.value - .9).coerceAtLeast(0.0).dp
        }
    }

    LaunchedEffect(listItemState.isOnSwipe.value) {
        onSwipeStatusChanged(listItemState.isOnSwipe.value)
    }

    LaunchedEffect(isEnabledToSwipe) {
        if (isEnabledToSwipe && listItemState.contentOffset.floatValue != 0f) {
            listItemState.swipeToZero()
        }
    }

    LaunchedEffect(isExpanded) {
        expandAnimation.animateTo(
            targetValue = if (isExpanded) 1f else 0f,
            animationSpec = expandedAnimationSpec()
        )
    }

    SwipeableListItem(
        modifier = modifier,
        state = listItemState,
        onClick = { onExpandedStatusChanged(!isExpanded) },
        leftActionIcon = {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = "pencil",
                modifier = Modifier.padding(16.dp)
            )
        },
        rightActionIcon = {
            Icon(
                imageVector = Icons.Rounded.DeleteForever,
                contentDescription = "trash can",
                modifier = Modifier.padding(16.dp)
            )
        },
        isEnabledToSwipe = isEnabledToSwipe,
        colors = SwipeableListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
                .mix(MaterialTheme.colorScheme.background)
                .ratio(listItemState.swipeOffsetRatio.value + expandAnimation.value),
            leftActionColors = SwipeableListItemDefaults.actionColors(
                containerColor = MaterialTheme.colorScheme.background,
                clickedContainerColor = Color.Blue,
                contentColor = MaterialTheme.colorScheme.onBackground,
                clickedContentColor = Color.White
            ),
            rightActionColors = SwipeableListItemDefaults.actionColors(
                containerColor = MaterialTheme.colorScheme.background,
                clickedContainerColor = Color.Red,
                contentColor = MaterialTheme.colorScheme.onBackground,
                clickedContentColor = Color.White
            )
        ),
        tonalElevation = elevation.value,
        shadow = SwipeableListItemDefaults.shadow(
            color = MaterialTheme.colorScheme.surface,
            alpha = 1 - (listItemState.swipeOffsetRatio.value / .1f + expandAnimation.value)
        )
    ) {
        Column {
            ListItem(
                headlineContent = {
                    if (bankCard.name.isNotBlank()) {
                        Text(
                            text = bankCard.name,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.MiddleEllipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = bankCard.getHiddenNumber().withSpaces(),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.MiddleEllipsis,
                            modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth()
                        )
                    }
                },
                supportingContent = {
                    if (bankCard.name.isNotBlank()) {
                        Text(
                            text = bankCard.getHiddenNumber().withSpaces(),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier
                            .scale(.7f)
                            .graphicsLayer { rotationZ = arrowDegrees.value }
                    )
                },
                trailingContent = {
                    bankCard.paymentSystem?.let {
                        PaymentSystemUtils.PaymentSystemImage(
                            paymentSystem = it,
                            maxWidth = 50.dp,
                        )
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                    headlineColor = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.fillMaxWidth()
            )


            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(expandedAnimationSpec()) +
                        fadeIn(expandedAnimationSpec()),
                exit = shrinkVertically(expandedAnimationSpec()) +
                        fadeOut(expandedAnimationSpec())
            ) {
                Column {
                    PlasticBankCard(
                        bankCard = bankCard,
                        onCopy = { part, text ->
                            scope.launch {
                                val clipData = ClipData.newPlainText("CardsScreen", text)
                                clipboard.setClipEntry(ClipEntry(clipData))

                                displayMessage(
                                    context.getString(
                                        R.string.card_part_text_is_copied,
                                        part.getDescription(context)
                                    )
                                )
                            }
                        },
                        onClick = onOpenCardDetails,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    TextButton(
                        onClick = onOpenCardDetails,
                        shape = SwipeableListItemDefaults.shape,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.open),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(VerdanaFont)
                        )
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun CardsScreenPreview() {
    CashbacksTheme(isDarkTheme = true) {
        CardsScreen(
            state = BankCardsState(
                cards = List(3) {
                    PrimaryBankCard(id = it.toLong(), number = "4422${it}2221111333$it")
                }.toImmutableList()
            ),
            snackbarHostState = remember { SnackbarHostState() },
            intentSender = IntentSender()
        )
    }
}


@Preview
@Composable
private fun BankCardListElementPreview() {
    CashbacksTheme(isDarkTheme = false) {
        BankCardListElement(
            bankCard = PrimaryBankCard(id = 0, number = "4422222211113333"),
            isExpanded = true
        )
    }
}