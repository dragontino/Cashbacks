package com.cashbacks.app.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cashbacks.app.util.animate

@Composable
fun EmptyList(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconModifier: Modifier = Modifier
) {
    val contentColor = MaterialTheme.colorScheme.onBackground.animate()

    Box(
        modifier = Modifier
            .then(modifier)
            .background(MaterialTheme.colorScheme.background.animate())
            .fillMaxSize(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(vertical = 20.dp)
                .align(Alignment.Center)
                .matchParentSize()
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = "empty list icon",
                    tint = contentColor,
                    modifier = iconModifier
                )
            }

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}