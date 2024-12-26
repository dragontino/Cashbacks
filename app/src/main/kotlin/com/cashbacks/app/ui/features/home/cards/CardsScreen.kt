package com.cashbacks.app.ui.features.home.cards

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Snackbar
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cashbacks.app.ui.composables.BankCard
import com.cashbacks.app.ui.composables.BasicFloatingActionButton
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.ConfirmDeletionDialog
import com.cashbacks.app.ui.composables.EditDeleteContent
import com.cashbacks.app.ui.composables.EmptyList
import com.cashbacks.app.ui.composables.SwipeableListItem
import com.cashbacks.app.ui.composables.SwipeableListItemDefaults
import com.cashbacks.app.ui.features.bankcard.BankCardArgs
import com.cashbacks.app.ui.features.home.HomeAppBarDefaults
import com.cashbacks.app.ui.features.home.HomeTopAppBar
import com.cashbacks.app.ui.features.home.HomeTopAppBarState
import com.cashbacks.app.ui.features.home.cards.mvi.CardsAction
import com.cashbacks.app.ui.features.home.cards.mvi.CardsEvent
import com.cashbacks.app.ui.managment.DialogType
import com.cashbacks.app.ui.managment.ListState
import com.cashbacks.app.ui.managment.rememberSwipeableListItemState
import com.cashbacks.app.ui.theme.CashbacksTheme
import com.cashbacks.app.ui.theme.VerdanaFont
import com.cashbacks.app.util.AnimationDefaults
import com.cashbacks.app.util.BankCardUtils.getDisplayableString
import com.cashbacks.app.util.BankCardUtils.hideNumber
import com.cashbacks.app.util.BankCardUtils.withSpaces
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.OnClick
import com.cashbacks.app.util.PaymentSystemUtils
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.mix
import com.cashbacks.app.util.reversed
import com.cashbacks.domain.R
import com.cashbacks.domain.model.BasicBankCard
import com.cashbacks.domain.model.PrimaryBankCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CardsScreen(
    viewModel: CardsViewModel,
    title: String,
    bottomPadding: Dp,
    openDrawer: () -> Unit,
    navigateToCard: (args: BankCardArgs) -> Unit,
    modifier: Modifier = Modifier
) {

    val snackbarHostState = remember(::SnackbarHostState)
    var dialogType: DialogType? by rememberSaveable { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is CardsEvent.NavigateToBankCard -> navigateToCard(event.args)
                is CardsEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is CardsEvent.OpenNavigationDrawer -> openDrawer()
                is CardsEvent.ChangeOpenedDialog -> dialogType = event.openedDialogType
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
                onConfirm = { viewModel.push(CardsAction.DeleteBankCard(card)) },
                onClose = { viewModel.push(CardsAction.HideDialog) }
            )
        }
    }

    CardsScreenContent(
        snackbarHostState = snackbarHostState,
        viewModel = viewModel,
        title = title,
        bottomPadding = bottomPadding,
        modifier = modifier
    )
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardsScreenContent(
    snackbarHostState: SnackbarHostState,
    viewModel: CardsViewModel,
    title: String,
    bottomPadding: Dp,
    modifier: Modifier = Modifier
) {
    val topBarState = rememberTopAppBarState()
    val lazyListState = rememberLazyListState()
    val fabHeightPx = rememberSaveable { mutableFloatStateOf(0f) }


    CollapsingToolbarScaffold(
        topBar = {
            HomeTopAppBar(
                title = title,
                state = viewModel.appBarState,
                onStateChange = {
                    viewModel.push(CardsAction.UpdateAppBarState(it))
                },
                searchPlaceholder = stringResource(R.string.search_cards_placeholder),
                onNavigationIconClick = { viewModel.push(CardsAction.ClickNavigationIcon) },
                colors = HomeAppBarDefaults.colors(
                    topBarContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = .6f)
                        .mix(MaterialTheme.colorScheme.primary)
                        .ratio(topBarState.overlappedFraction)
                )
            )
        },
        topBarState = topBarState,
        contentState = lazyListState,
        topBarScrollEnabled = viewModel.appBarState is HomeTopAppBarState.TopBar,
        floatingActionButtons = {
            AnimatedVisibility(visible = viewModel.appBarState is HomeTopAppBarState.TopBar) {
                BasicFloatingActionButton(icon = Icons.Rounded.Add) {
                    viewModel.push(CardsAction.CreateBankCard)
                }
            }
        },
        fabModifier = Modifier
            .padding(bottom = bottomPadding)
            .windowInsetsPadding(WindowInsets.tappableElement.only(WindowInsetsSides.End))
            .onGloballyPositioned { fabHeightPx.floatValue = it.size.height.toFloat() },
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
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
    ) {
        val cardsState = viewModel.cardsFlow.collectAsStateWithLifecycle()
        Crossfade(
            targetState = ListState.fromList(cardsState.value),
            label = "loading animation",
            animationSpec = tween(durationMillis = 150, easing = LinearEasing),
            modifier = Modifier.fillMaxSize(),
        ) { listState ->
            when (listState) {
                is ListState.Loading -> {
                    LoadingInBox(modifier = Modifier.padding(bottom = bottomPadding))
                }

                ListState.Empty -> {
                    EmptyList(
                        text = when (val appBarState = viewModel.appBarState) {
                            is HomeTopAppBarState.Search -> {
                                if (appBarState.query.isBlank()) {
                                    stringResource(R.string.empty_search_query)
                                } else {
                                    stringResource(R.string.empty_search_results)
                                }
                            }

                            is HomeTopAppBarState.TopBar -> stringResource(R.string.empty_bank_cards_list)
                        },
                        modifier = Modifier.padding(
                            bottom = with(LocalDensity.current) {
                                bottomPadding + fabHeightPx.floatValue.toDp()
                            }
                        )
                    )
                }

                is ListState.Stable -> {
                    LazyColumn(
                        state = lazyListState,
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(listState.data) { card ->
                            BankCardListElement(
                                bankCard = card,
                                onItemClick = viewModel::onItemClick,
                                pushAction = viewModel::push,
                                isExpanded = viewModel.expandedCardId == card.id,
                                isSwiped = viewModel.swipedCardId == card.id,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        item {
                            Spacer(
                                modifier = with(LocalDensity.current) {
                                    Modifier.height((bottomPadding + fabHeightPx.floatValue.toDp()).animate())
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun BankCardListElement(
    bankCard: PrimaryBankCard,
    onItemClick: (OnClick) -> Unit,
    pushAction: (CardsAction) -> Unit,
    isExpanded: Boolean,
    isSwiped: Boolean,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val state = rememberSwipeableListItemState()
    val expandAnimation = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(isSwiped) {
        if (isSwiped != state.isSwiped.value) {
            state.swipe()
        }
    }

    LaunchedEffect(isExpanded) {
        expandAnimation.animateTo(
            targetValue = if (isExpanded) 1f else 0f,
            animationSpec = AnimationDefaults.expandedAnimationSpec()
        )
    }

    LaunchedEffect(state.isSwiped.value) {
        if (state.isSwiped.value != isSwiped) {
            pushAction(
                CardsAction.SwipeCard(
                    isSwiped = state.isSwiped.value,
                    cardId = bankCard.id
                )
            )
        }
    }

    SwipeableListItem(
        modifier = modifier,
        state = state,
        hiddenContent = {
            EditDeleteContent(
                onEditClick = {
                    pushAction(CardsAction.EditBankCard(bankCard.id))
                    pushAction(CardsAction.SwipeCard(isSwiped = false))
                },
                onDeleteClick = {
                    pushAction(CardsAction.ShowDialog(DialogType.ConfirmDeletion(bankCard)))
                    pushAction(CardsAction.SwipeCard(isSwiped = false))
                }
            )
        },
        onClick = {
            pushAction(
                CardsAction.ExpandCard(isExpanded = isExpanded.not(), cardId = bankCard.id)
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
                            text = bankCard.hideNumber().withSpaces(),
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
                            text = bankCard.hideNumber().withSpaces(),
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
                            pushAction(
                                CardsAction.SwipeCard(
                                    isSwiped = isSwiped.not(),
                                    cardId = bankCard.id
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
                enter = expandVertically(AnimationDefaults.expandedAnimationSpec()) +
                        fadeIn(AnimationDefaults.expandedAnimationSpec()),
                exit = shrinkVertically(AnimationDefaults.expandedAnimationSpec()) +
                        fadeOut(AnimationDefaults.expandedAnimationSpec())
            ) {
                Column {
                    BankCard(
                        bankCard = bankCard,
                        onCopy = { part, text ->
                            onItemClick {
                                clipboardManager.setText(AnnotatedString(text))
                                pushAction(
                                    CardsAction.ShowSnackbar(
                                        context.getString(
                                            R.string.card_part_text_is_copied,
                                            part.getDescription(context)
                                        )
                                    )
                                )
                            }
                        },
                        onClick = { pushAction(CardsAction.OpenBankCardDetails(bankCard.id)) },
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    TextButton(
                        onClick = { pushAction(CardsAction.OpenBankCardDetails(bankCard.id)) },
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
private fun CardsContentScreenPreview() {
    CashbacksTheme(isDarkTheme = false) {
        BankCardListElement(
            bankCard = PrimaryBankCard(id = 0, number = "4422222211113333"),
            onItemClick = {},
            pushAction = {},
            isSwiped = false,
            isExpanded = true
        )
    }
}