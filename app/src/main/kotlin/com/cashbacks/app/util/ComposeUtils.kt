package com.cashbacks.app.util

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

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



@Composable
fun LoadingInBox(
    modifier: Modifier = Modifier,
    loadingModifier: Modifier = Modifier.scale(1.4f),
    progress: Float? = null,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(containerColor.animate())
            .fillMaxSize(),
    ) {
        Loading(progress = progress, modifier = loadingModifier, color = contentColor)
    }
}


@Composable
fun Loading(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    color: Color = MaterialTheme.colorScheme.primary
) = when (progress) {
    null -> {
        CircularProgressIndicator(
            color = color.animate(),
            strokeCap = StrokeCap.Round,
            strokeWidth = 3.5.dp,
            modifier = modifier
        )
    }

    else -> {
        CircularProgressIndicator(
            progress = { progress },
            modifier = modifier,
            color = color.animate(),
            strokeWidth = 3.5.dp,
            strokeCap = StrokeCap.Round,
        )
    }
}



suspend fun LazyListState.smoothScrollToItem(targetPosition: Int) {
    val itemsToScroll = targetPosition - firstVisibleItemIndex
    val itemSize = with (layoutInfo.visibleItemsInfo) {
        if (isEmpty()) 0 else this[0].size
    }

    val pixelsToScroll = when {
        itemsToScroll < layoutInfo.visibleItemsInfo.size -> layoutInfo
            .visibleItemsInfo
            .slice(0..<itemsToScroll)
            .sumOf { it.size } - firstVisibleItemScrollOffset

        else -> itemSize * itemsToScroll
    }


    val duration = when {
        itemsToScroll < layoutInfo.visibleItemsInfo.size -> layoutInfo
            .visibleItemsInfo
            .slice(0..<itemsToScroll)
            .fold(0) { acc, item ->
                (acc + item.size * 1.2).roundToInt()
            }

        else -> (itemsToScroll * itemSize * 1.2).roundToInt()
    }

    animateScrollBy(
        value = pixelsToScroll.toFloat(),
        animationSpec = tween(
            durationMillis = duration,
            delayMillis = 60,
            easing = FastOutSlowInEasing
        )
    )
}



@ExperimentalLayoutApi
@Composable
fun keyboardAsState(): State<Boolean> {
    val isImeVisible = WindowInsets.isImeVisible
    return rememberUpdatedState(newValue = isImeVisible)
}


fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}


val Color.reversed get() = copy(red = 1 - red, green = 1 - green, blue = 1 - blue)

data class ColorPair(val firstColor: Color, val secondColor: Color) {
    infix fun ratio(ratio: Float): Color {
        val secondColorRatio = 1 - ratio

        fun calculateComponent(getComponent: Color.() -> Float): Float {
            val first = firstColor.getComponent() * ratio
            val second = secondColor.getComponent() * secondColorRatio
            return first + second
        }

        return Color(
            alpha = calculateComponent { alpha },
            red = calculateComponent { red },
            green = calculateComponent { green },
            blue = calculateComponent { blue }
        )
    }
}

infix fun Color.mix(otherColor: Color) =
    ColorPair(firstColor = this, secondColor = otherColor)


@Composable
fun <T, R> T.composableLet(block: @Composable (T) -> R): @Composable () -> R = {
    block(this)
}


@Composable
fun <R> composableBlock(
    condition: Boolean,
    block: @Composable () -> R
): @Composable (() -> R)? = when {
    condition -> block
    else -> null
}