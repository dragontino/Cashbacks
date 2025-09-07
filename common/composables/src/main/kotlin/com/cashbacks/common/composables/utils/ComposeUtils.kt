package com.cashbacks.common.composables.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt


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


@Stable
val Color.reversed get() = copy(red = 1 - red, green = 1 - green, blue = 1 - blue)

@Immutable
data class ColorPair(val firstColor: Color, val secondColor: Color) {
    @Stable
    infix fun ratio(ratio: Float): Color {
        val ratio = ratio.coerceIn(0f..1f)
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

@Stable
infix fun Color.mix(otherColor: Color) =
    ColorPair(firstColor = this, secondColor = otherColor)


@Stable
@Composable
fun <T, R> T.composableLet(block: @Composable (T) -> R): @Composable () -> R = {
    block(this)
}


@Stable
@Composable
fun <R> composableBlock(
    condition: Boolean,
    block: @Composable () -> R
): @Composable (() -> R)? = when {
    condition -> block
    else -> null
}


fun <T, S : Any> mutableStateSaver(valueSaver: Saver<T, S>) = Saver<MutableState<T>, S>(
    save = { state ->
        with(valueSaver) { save(state.value) }
    },
    restore = { saved ->
        valueSaver.restore(saved)?.let(::mutableStateOf)
    }
)


inline val Dp.Companion.Saver: Saver<Dp, Float>
    get() = Saver(
        save = { it.value },
        restore = { it.dp }
    )