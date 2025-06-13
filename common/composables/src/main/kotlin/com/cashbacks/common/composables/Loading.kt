package com.cashbacks.common.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.cashbacks.common.composables.utils.animate

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