package com.cashbacks.common.composables.utils

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

@Stable
fun <T> loadingContentAnimationSpec() = tween<T>(
    durationMillis = 150,
    easing = FastOutSlowInEasing
)

@Stable
fun <T> expandedAnimationSpec(): TweenSpec<T> = tween<T>(
    durationMillis = 350,
    easing = FastOutSlowInEasing
)

@Stable
fun floatingActionButtonEnterAnimation(durationMillis: Int = 500) = slideInVertically(
    animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
    initialOffsetY = { it }
) + fadeIn(
    animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
)

@Stable
fun floatingActionButtonExitAnimation(durationMillis: Int = 500) = slideOutVertically(
    animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
    targetOffsetY = { it }
) + fadeOut(
    animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
)


@Composable
fun Color.animate(durationMillis: Int = 400): Color =
    animateColorAsState(
        targetValue = this,
        animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
        label = "colorAnimation"
    ).value



@Composable
fun Dp.animate(durationMillis: Int = 400): Dp =
    animateDpAsState(
        targetValue = this,
        animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
        label = "dpAnimation"
    ).value