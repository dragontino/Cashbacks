package com.cashbacks.app.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.layout.union
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cashbacks.app.util.animate

@Composable
fun CollapsingToolbarScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit) = {},
    floatingActionButton: @Composable (() -> Unit) = {},
    fabPosition: FabPosition = FabPosition.Center,
    snackbarHost: @Composable (() -> Unit) = {},
    contentWindowInsets: WindowInsets = CollapsingToolbarScaffoldDefaults.contentWindowInsets(),
    content: @Composable ((PaddingValues) -> Unit),
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        floatingActionButton = {
            floatingActionButton()
            /*Box(
                modifier = Modifier.windowInsetsPadding(contentWindowInsets),
                contentAlignment = Alignment.Center,
                content = { floatingActionButton() }
            )*/
        },
        floatingActionButtonPosition = fabPosition,
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