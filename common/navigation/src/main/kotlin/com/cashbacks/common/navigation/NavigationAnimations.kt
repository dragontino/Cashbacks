package com.cashbacks.common.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.Alignment
import com.cashbacks.common.utils.AnimationDefaults
import kotlin.math.roundToInt

fun enterScreenTransition(
    expandFrom: Alignment.Horizontal,
    animationTime: Int = AnimationDefaults.SCREEN_DELAY_MILLIS,
    delayTimePercent: Float = .05f,
): EnterTransition {
    val delayMillis = (animationTime * delayTimePercent).roundToInt()
    val durationMillis = animationTime - delayMillis
    return slideInHorizontally(
        animationSpec = tween(durationMillis, delayMillis, easing = FastOutSlowInEasing)
    ) { fullWidth ->
        when (expandFrom) {
            Alignment.Start -> -fullWidth
            Alignment.End -> fullWidth
            else -> 0
        }
    }
}


fun exitScreenTransition(
    shrinkTowards: Alignment.Horizontal,
    animationTime: Int = AnimationDefaults.SCREEN_DELAY_MILLIS,
    delayTimePercent: Float = .2f
): ExitTransition {
    val delayMillis = (animationTime * delayTimePercent).roundToInt()
    val durationMillis = animationTime - delayMillis
    return slideOutHorizontally(
        animationSpec = tween(durationMillis, delayMillis, easing = FastOutSlowInEasing),
    ) { fullWidth ->
        when (shrinkTowards) {
            Alignment.Start -> -fullWidth
            else -> fullWidth
        }
    }
}