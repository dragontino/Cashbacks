package com.cashbacks.common.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.cashbacks.common.composables.utils.animate

@Composable
fun BasicFloatingActionButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    border: BorderStroke? = BorderStroke(
        width = 2.dp,
        color = MaterialTheme.colorScheme.onPrimary.animate()
    ),
    onClick: () -> Unit
) {
    val borderModifier = when (border) {
        null -> Modifier
        else -> Modifier.border(border, shape = FloatingActionButtonDefaults.shape)
    }

    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primaryContainer.animate(),
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer.animate(),
        elevation = FloatingActionButtonDefaults.loweredElevation(),
        modifier = Modifier
            .then(modifier)
            .then(borderModifier)
    ) {
        Icon(imageVector = icon, contentDescription = null)
    }
}