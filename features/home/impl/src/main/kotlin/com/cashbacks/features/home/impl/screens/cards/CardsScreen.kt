package com.cashbacks.features.home.impl.screens.cards

import android.content.ClipData
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.cashbacks.common.composables.swipeable.EditDeleteActions
import com.cashbacks.common.composables.swipeable.SwipeableListItem
import com.cashbacks.common.composables.swipeable.SwipeableListItemDefaults
import com.cashbacks.common.composables.swipeable.rememberSwipeableListItemState
import com.cashbacks.common.composables.theme.CashbacksTheme
import com.cashbacks.common.composables.theme.VerdanaFont
import com.cashbacks.common.composables.utils.animate
import com.cashbacks.common.composables.utils.expandedAnimationSpec
import com.cashbacks.common.composables.utils.mix
import com.cashbacks.common.composables.utils.reversed
import com.cashbacks.common.resources.R
import com.cashbacks.features.bankcard.domain.model.BankCard
import com.cashbacks.features.bankcard.domain.model.BasicBankCard
import com.cashbacks.features.bankcard.domain.model.PrimaryBankCard
import com.cashbacks.features.bankcard.domain.utils.BankCardUtils.getHiddenNumber
import com.cashbacks.features.bankcard.domain.utils.BankCardUtils.withSpaces
import com.cashbacks.features.bankcard.presentation.api.BankCardArgs
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
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun BankCardsRoot(
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
        sendIntent = viewModel::sendIntent,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardsScreen(
    state: BankCardsState,
    snackbarHostState: SnackbarHostState,
    sendIntent: (BankCardsIntent) -> Unit,
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
                    sendIntent(BankCardsIntent.ChangeAppBarState(it))
                },
                searchPlaceholder = stringResource(R.string.search_cards_placeholder),
                onNavigationIconClick = { sendIntent(BankCardsIntent.ClickNavigationButton) },
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
                    sendIntent(BankCardsIntent.CreateBankCard)
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
            sendIntent = sendIntent,
        )
    }
}


@Composable
private fun BankCardList(
    state: BankCardsState,
    contentState: LazyListState,
    contentPadding: PaddingValues,
    sendIntent: (BankCardsIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Crossfade(
        targetState = state.cards.toListState(),
        label = "loading animation",
        animationSpec = tween(durationMillis = 150, easing = LinearEasing),
        modifier = modifier.fillMaxSize(),
    ) { listState ->
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

                    itemsIndexed(listState.data) { index, card ->
                        BankCardListElement(
                            bankCard = card,
                            position = index,
                            sendIntent = sendIntent,
                            isExpanded = state.expandedCardIndex == index,
                            isSwiped = state.swipedCardIndex == index,
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


@Composable
private fun BankCardListElement(
    bankCard: BankCard,
    position: Int,
    sendIntent: (BankCardsIntent) -> Unit,
    isExpanded: Boolean,
    isSwiped: Boolean,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val state = rememberSwipeableListItemState()
    val scope = rememberCoroutineScope()
    val expandAnimation = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(isSwiped) {
        if (isSwiped != state.isSwiped.value) {
            state.swipe()
        }
    }

    LaunchedEffect(isExpanded) {
        expandAnimation.animateTo(
            targetValue = if (isExpanded) 1f else 0f,
            animationSpec = expandedAnimationSpec()
        )
    }

    LaunchedEffect(state.isSwiped.value) {
        if (state.isSwiped.value != isSwiped) {
            sendIntent(
                BankCardsIntent.SwipeCard(
                    position = position,
                    isSwiped = state.isSwiped.value
                )
            )
        }
    }

    SwipeableListItem(
        modifier = modifier,
        state = state,
        actions = {
            EditDeleteActions(
                onEditClick = {
                    sendIntent(BankCardsIntent.EditBankCard(bankCard.id))
                    sendIntent(BankCardsIntent.SwipeCard(null))
                },
                onDeleteClick = {
                    sendIntent(BankCardsIntent.OpenDialog(DialogType.ConfirmDeletion(bankCard)))
                    sendIntent(BankCardsIntent.SwipeCard(null))
                }
            )
        },
        onClick = {
            sendIntent(
                BankCardsIntent.ExpandCard(position, isExpanded = isExpanded.not())
            )
        },
        clickIndication = null,
        border = with(SwipeableListItemDefaults) {
            val colors = borderColors.map { it.copy(alpha = 1 - expandAnimation.value) }
            borderStroke.copy(brush = borderBrush(colors))
        },
        containerColor = MaterialTheme.colorScheme.surface
            .mix(MaterialTheme.colorScheme.background)
            .ratio(expandAnimation.value)
    ) {
        Column {
            ListItem(
                headlineContent = {
                    if (bankCard.name.isNotBlank()) {
                        Text(
                            text = bankCard.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = bankCard.getHiddenNumber().withSpaces(),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.horizontalScroll(rememberScrollState())
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
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        )
                    }
                },
                leadingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                            contentDescription = null,
                            modifier = Modifier
                                .scale(.7f)
                                .graphicsLayer {
                                    rotationZ = expandAnimation.value * 90f
                                }
                        )

                        Spacer(Modifier.width(16.dp))

                        bankCard.paymentSystem?.let {
                            PaymentSystemUtils.PaymentSystemImage(it, maxWidth = 35.dp)
                        }
                    }
                },
                trailingContent = {
                    IconButton(
                        onClick = {
                            sendIntent(
                                BankCardsIntent.SwipeCard(
                                    position = position,
                                    isSwiped = isSwiped.not()
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "show options"
                        )
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                    headlineColor = MaterialTheme.colorScheme.onBackground
                )
            )


            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(expandedAnimationSpec()) +
                        fadeIn(expandedAnimationSpec()),
                exit = shrinkVertically(expandedAnimationSpec()) +
                        fadeOut(expandedAnimationSpec())
            ) {
                Column {
                    com.cashbacks.features.bankcard.presentation.api.composables.BankCard(
                        bankCard = bankCard,
                        onCopy = { part, text ->
                            scope.launch {
                                val clipData = ClipData.newPlainText("CardsScreen", text)
                                clipboard.setClipEntry(ClipEntry(clipData))
                            }
                            sendIntent(
                                BankCardsIntent.DisplayMessage(
                                    context.getString(
                                        R.string.card_part_text_is_copied,
                                        part.getDescription(context)
                                    )
                                )
                            )
                        },
                        onClick = { sendIntent(BankCardsIntent.OpenBankCardDetails(bankCard.id)) },
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    TextButton(
                        onClick = { sendIntent(BankCardsIntent.OpenBankCardDetails(bankCard.id)) },
                        shape = MaterialTheme.shapes.medium,
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
                    PrimaryBankCard(id = 0, number = "4422222211113333")
                }
            ),
            snackbarHostState = remember { SnackbarHostState() },
            sendIntent = {}
        )
    }
}


@Preview
@Composable
private fun CardsContentScreenPreview() {
    CashbacksTheme(isDarkTheme = false) {
        BankCardListElement(
            bankCard = PrimaryBankCard(id = 0, number = "4422222211113333"),
            position = 1,
            sendIntent = {},
            isSwiped = false,
            isExpanded = true
        )
    }
}