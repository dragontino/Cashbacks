package com.cashbacks.app.util

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

object AnimationDefaults {
    const val SCREEN_DELAY_MILLIS: Int = 650
}

fun floatingActionButtonEnterAnimation(durationMillis: Int = 500) = slideInVertically(
    animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
    initialOffsetY = { it }
) + fadeIn(
    animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
)

fun floatingActionButtonExitAnimation(durationMillis: Int = 500) = slideOutVertically(
    animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
    targetOffsetY = { it }
) + fadeOut(
    animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
)