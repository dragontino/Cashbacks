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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
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
    snackbarHost: @Composable (() -> Unit) = {},
    contentWindowInsets: WindowInsets = CollapsingToolbarScaffoldDefaults.contentWindowInsets(),
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

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.tappableElement)
                    .padding(16.dp)
                    .align(Alignment.BottomEnd),
                content = floatingActionButtons
            )
        }
    }
}



object CollapsingToolbarScaffoldDefaults {

    @Composable
    fun contentWindowInsets() = WindowInsets
        .tappableElement.only(WindowInsetsSides.Bottom)
        .union(
            WindowInsets.ime.only(WindowInsetsSides.Bottom)
        )
}