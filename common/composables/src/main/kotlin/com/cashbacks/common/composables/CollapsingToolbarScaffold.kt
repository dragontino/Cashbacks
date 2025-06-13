package com.cashbacks.common.composables

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingToolbarScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit) = {},
    topBarState: TopAppBarState = rememberTopAppBarState(initialHeightOffsetLimit = -100f),
    contentState: ScrollableState = rememberScrollState(),
    topBarScrollEnabled: Boolean = true,
    floatingActionButtons: @Composable (ColumnScope.() -> Unit) = {},
    fabModifier: Modifier = Modifier.windowInsetsPadding(
        WindowInsets.tappableElement.only(WindowInsetsSides.Horizontal + WindowInsetsSides.End)
    ),
    snackbarHost: @Composable (() -> Unit) = {},
    contentWindowInsets: WindowInsets = CollapsingToolbarScaffoldDefaults.contentWindowInsets,
    content: @Composable (() -> Unit),
) {
    val scope = rememberCoroutineScope()
    val nestedScrollConnection = remember(
        topBarState,
        contentState.canScrollForward,
        contentState.canScrollBackward,
        topBarScrollEnabled
    ) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < 0 && !contentState.canScrollForward) return Offset.Zero

                val previousContentOffset = topBarState.contentOffset

                if (topBarScrollEnabled) {
                    topBarState.heightOffset = (topBarState.heightOffset + delta)
                        .coerceIn(topBarState.heightOffsetLimit, 0f)
                }

                if (delta > 0 && !contentState.canScrollBackward || delta < 0) {
                    topBarState.contentOffset = (topBarState.contentOffset + delta)
                        .coerceIn(topBarState.heightOffsetLimit, 0f)
                }

                return when (topBarState.contentOffset) {
                    previousContentOffset -> Offset.Zero
                    else -> available.copy(x = 0f)
                }
            }


            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                scope.launch {
                    animate(
                        initialValue = topBarState.heightOffset,
                        targetValue = when {
                            topBarState.collapsedFraction < .5f -> 0f
                            else -> topBarState.heightOffsetLimit
                        },
                        animationSpec = tween(durationMillis = 300, easing = LinearEasing)
                    ) { value, _ ->
                        topBarState.heightOffset = value
                    }
                }

                scope.launch {
                    animate(
                        initialValue = topBarState.contentOffset,
                        targetValue = when {
                            topBarState.overlappedFraction < .5f -> 0f
                            else -> topBarState.heightOffsetLimit
                        },
                        animationSpec = tween(durationMillis = 300, easing = LinearEasing)
                    ) { value, _ ->
                        topBarState.contentOffset = value
                    }
                }
                return Velocity.Zero
            }
        }
    }


    Scaffold(
        modifier = modifier,
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
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) { contentPadding ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .padding(contentPadding)
                .nestedScroll(nestedScrollConnection)
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(x = 0, y = topBarState.heightOffset.roundToInt()) }
                    .zIndex(3f)
                    .onGloballyPositioned {
                        topBarState.heightOffsetLimit = -it.size.height.toFloat()
                    }
                    .windowInsetsPadding(contentWindowInsets.only(WindowInsetsSides.Horizontal))
            ) {
                topBar()
            }

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = 0,
                            y = (topBarState.contentOffset - topBarState.heightOffsetLimit).roundToInt()
                        )
                    }
                    .zIndex(2f)
            ) {
                content()
            }
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