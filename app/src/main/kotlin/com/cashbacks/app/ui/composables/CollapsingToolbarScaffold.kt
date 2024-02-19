package com.cashbacks.app.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.unit.dp
import com.cashbacks.app.util.animate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingToolbarScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit) = {},
    topBarState: TopAppBarState = rememberTopAppBarState(
        initialContentOffset = 100f
    ),
    floatingActionButtons: @Composable (ColumnScope.() -> Unit) = {},
    fabModifier: Modifier = Modifier.windowInsetsPadding(
        WindowInsets.tappableElement.only(WindowInsetsSides.Horizontal + WindowInsetsSides.End)
    ),
    snackbarHost: @Composable (() -> Unit) = {},
    contentWindowInsets: WindowInsets = CollapsingToolbarScaffoldDefaults.contentWindowInsets,
    content: @Composable ((PaddingValues) -> Unit),
) {
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {

        }
    }


    Scaffold(
        modifier = modifier,
        topBar = topBar,
        snackbarHost = snackbarHost,
        contentWindowInsets = contentWindowInsets,
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End,
                modifier = fabModifier,
                content = floatingActionButtons
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = MaterialTheme.colorScheme.background.animate(),
        contentColor = MaterialTheme.colorScheme.onBackground.animate(),
    ) { contentPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            content(contentPadding)

            /*Box(
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                topBar()
            }*/
        }
    }
}



object CollapsingToolbarScaffoldDefaults {

    val contentWindowInsets: WindowInsets
        @Composable
        get() = WindowInsets
            .tappableElement.only(WindowInsetsSides.Bottom + WindowInsetsSides.End)
            .union(WindowInsets.ime.only(WindowInsetsSides.Bottom))
}