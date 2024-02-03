package com.cashbacks.app.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cashbacks.app.R
import com.cashbacks.app.ui.composables.BankCardCompose
import com.cashbacks.app.ui.composables.BasicFloatingActionButton
import com.cashbacks.app.ui.composables.CollapsingToolbarScaffold
import com.cashbacks.app.ui.composables.EmptyList
import com.cashbacks.app.ui.managment.ListState
import com.cashbacks.app.ui.screens.navigation.AppScreens
import com.cashbacks.app.ui.theme.CashbacksTheme
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.viewmodel.CardsViewModel
import com.cashbacks.domain.model.BankCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen(
    viewModel: CardsViewModel,
    openDrawer: () -> Unit,
    navigateTo: (route: String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember(::SnackbarHostState)

    CollapsingToolbarScaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = AppScreens.BankCards.title(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = openDrawer) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = "open menu"
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
            BasicFloatingActionButton(
                icon = Icons.Rounded.Add,
                onClick = {
                    navigateTo(AppScreens.BankCardEditor.createUrl(null))
                },
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    containerColor = MaterialTheme.colorScheme.onBackground.animate(),
                    contentColor = MaterialTheme.colorScheme.background.animate(),
                    shape = MaterialTheme.shapes.medium
                )
            }
        }
    ) { contentPadding ->

        Crossfade(
            targetState = viewModel.state.value,
            label = "loading animation",
            animationSpec = tween(durationMillis = 150, easing = LinearEasing),
            modifier = Modifier.padding(contentPadding)
        ) { state ->
            when (state) {
                ListState.Loading -> LoadingInBox()
                ListState.Empty -> EmptyList(text = stringResource(R.string.empty_bank_cards_list))
                ListState.Stable -> CardsContentScreen(
                    cards = viewModel.cards.value,
                    navigateTo = navigateTo,
                    showSnackbar = remember {
                        { message -> scope.launch { snackbarHostState.showSnackbar(message) } }
                    }
                )
            }
        }


    }
}



@Composable
private fun CardsContentScreen(
    cards: List<BankCard>,
    navigateTo: (route: String) -> Unit,
    showSnackbar: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current


    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        items(cards) {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface.animate(),
                    contentColor = MaterialTheme.colorScheme.onBackground.animate()
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                BankCardCompose(
                    bankCard = it,
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
                    onClick = { navigateTo(AppScreens.BankCardViewer.createUrl(it.id)) },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(stringResource(R.string.open), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}


@Preview
@Composable
private fun CardsContentScreenPreview() {
    CashbacksTheme(isDarkTheme = false) {
        CardsContentScreen(
            cards = listOf(
                BankCard(id = 0, number = "442222")
            ),
            navigateTo = {},
            showSnackbar = {}
        )
    }
}