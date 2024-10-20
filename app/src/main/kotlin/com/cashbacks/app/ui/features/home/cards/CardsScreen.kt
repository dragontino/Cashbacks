package com.cashbacks.app.ui.features.home.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cashbacks.app.ui.composables.BankCard
import com.cashbacks.app.ui.composables.BasicFloatingActionButton
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.EmptyList
import com.cashbacks.app.ui.features.bankcard.BankCardArgs
import com.cashbacks.app.ui.features.home.HomeTopAppBar
import com.cashbacks.app.ui.features.home.HomeTopAppBarState
import com.cashbacks.app.ui.features.home.cards.mvi.CardsAction
import com.cashbacks.app.ui.features.home.cards.mvi.CardsEvent
import com.cashbacks.app.ui.managment.ListState
import com.cashbacks.app.ui.theme.CashbacksTheme
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.OnClick
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.reversed
import com.cashbacks.domain.R
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

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is CardsEvent.NavigateToBankCard -> navigateToCard(event.args)
                is CardsEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is CardsEvent.OpenNavigationDrawer -> openDrawer()
            }
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
                onNavigationIconClick = { viewModel.push(CardsAction.ClickNavigationIcon) }
            )
        },
        topBarState = topBarState,
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
                    CardsContentScreen(
                        cards = listState.data,
                        state = lazyListState,
                        bottomPadding = with(LocalDensity.current) {
                            (bottomPadding + fabHeightPx.floatValue.toDp()).animate()
                        },
                        pushAction = viewModel::push,
                        onItemClick = viewModel::onItemClick
                    )
                }
            }
        }
    }
}



@Composable
private fun CardsContentScreen(
    cards: List<PrimaryBankCard>,
    state: LazyListState,
    bottomPadding: Dp,
    pushAction: (CardsAction) -> Unit,
    onItemClick: (OnClick) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current


    LazyColumn(
        state = state,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        items(cards) { bankCard ->
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface.animate(),
                    contentColor = MaterialTheme.colorScheme.onBackground.animate()
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = bankCard.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth()
                )

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
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        pushAction(
                            CardsAction.OpenBankCardDetails(cardId = bankCard.id)
                        )
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                ) {
                    Text(stringResource(R.string.open), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(bottomPadding))
        }
    }
}


@Preview
@Composable
private fun CardsContentScreenPreview() {
    CashbacksTheme(isDarkTheme = false) {
        CardsContentScreen(
            cards = listOf(
                PrimaryBankCard(id = 0, number = "4422222211113333")
            ),
            state = rememberLazyListState(),
            bottomPadding = 40.dp,
            pushAction = {},
            onItemClick = {}
        )
    }
}