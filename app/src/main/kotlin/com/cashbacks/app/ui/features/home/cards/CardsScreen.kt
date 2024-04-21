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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cashbacks.domain.R
import com.cashbacks.app.ui.composables.BankCardCompose
import com.cashbacks.app.ui.composables.BasicFloatingActionButton
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.EmptyList
import com.cashbacks.app.ui.features.bankcard.BankCardArgs
import com.cashbacks.app.ui.features.home.HomeTopAppBar
import com.cashbacks.app.ui.features.home.HomeTopAppBarState
import com.cashbacks.app.ui.managment.ListState
import com.cashbacks.app.ui.managment.ScreenEvents
import com.cashbacks.app.ui.theme.CashbacksTheme
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.reversed
import com.cashbacks.domain.model.BankCard
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()

    val topBarState = rememberTopAppBarState()
    val lazyListState = rememberLazyListState()
    val snackbarHostState = remember(::SnackbarHostState)

    val showSnackbar = remember {
        fun(message: String) {
            scope.launch { snackbarHostState.showSnackbar(message) }
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventsFlow.collect { event ->
            if (event is ScreenEvents.Navigate) {
                (event.args as? BankCardArgs)?.let(navigateToCard)
            } else if (event is ScreenEvents.ShowSnackbar) {
                event.message.let(showSnackbar)
            }
        }
    }

    val fabPaddingDp = rememberSaveable { mutableFloatStateOf(0f) }

    CollapsingToolbarScaffold(
        topBar = {
            HomeTopAppBar(
                title = title,
                query = viewModel.query.value,
                onQueryChange = viewModel.query::value::set,
                state = viewModel.appBarState,
                onStateChange = viewModel::appBarState::set,
                searchPlaceholder = stringResource(R.string.search_cards_placeholder),
                onNavigationIconClick = openDrawer
            )
        },
        topBarState = topBarState,
        contentState = lazyListState,
        topBarContainerColor = when (viewModel.appBarState) {
            HomeTopAppBarState.Search -> Color.Unspecified
            HomeTopAppBarState.TopBar -> MaterialTheme.colorScheme.primary
        },
        floatingActionButtons = {
            AnimatedVisibility(visible = !viewModel.isSearch) {
                BasicFloatingActionButton(icon = Icons.Rounded.Add) {
                    navigateToCard(BankCardArgs.New)
                }
            }
        },
        fabModifier = Modifier
            .padding(bottom = bottomPadding)
            .windowInsetsPadding(WindowInsets.tappableElement.only(WindowInsetsSides.End))
            .graphicsLayer {
                fabPaddingDp.floatValue = size.height.toDp().value
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
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
    ) {
        Crossfade(
            targetState = viewModel.state.value,
            label = "loading animation",
            animationSpec = tween(durationMillis = 150, easing = LinearEasing),
            modifier = Modifier.fillMaxSize(),
        ) { state ->
            when (state) {
                ListState.Loading -> LoadingInBox(
                    modifier = Modifier.padding(bottom = bottomPadding)
                )
                ListState.Empty -> EmptyList(
                    text = when {
                        viewModel.isSearch -> {
                            when {
                                viewModel.query.value.isBlank() -> stringResource(R.string.empty_search_query)
                                else -> stringResource(R.string.empty_search_results)
                            }
                        }
                        else -> stringResource(R.string.empty_bank_cards_list)
                    },
                    modifier = Modifier.padding(bottom = bottomPadding)
                )
                ListState.Stable -> CardsContentScreen(
                    cards = viewModel.cards,
                    state = lazyListState,
                    navigateToCard = { viewModel.navigateTo(it) },
                    showSnackbar = viewModel::showSnackbar,
                    bottomPadding = bottomPadding + fabPaddingDp.floatValue.dp
                )
            }
        }
    }
}



@Composable
private fun CardsContentScreen(
    cards: List<BankCard>,
    state: LazyListState,
    navigateToCard: (args: BankCardArgs) -> Unit,
    showSnackbar: (String) -> Unit,
    bottomPadding: Dp
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

                BankCardCompose(
                    bankCard = bankCard,
                    onCopy = remember {
                        {
                            clipboardManager.setText(AnnotatedString(it))
                            showSnackbar(context.getString(R.string.text_is_copied))
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        navigateToCard(BankCardArgs.Existing(id = bankCard.id, isEditing = false))
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
                BankCard(id = 0, number = "4422222211113333")
            ),
            state = rememberLazyListState(),
            navigateToCard = {},
            showSnackbar = {},
            bottomPadding = 40.dp
        )
    }
}