package com.cashbacks.common.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cashbacks.common.composables.utils.reversed

@Composable
fun BoundedSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    maxLines: Int = 3,
    actionOnNewLine: Boolean = true,
    shape: Shape = MaterialTheme.shapes.medium,
    containerColor: Color = MaterialTheme.colorScheme.background.reversed,
    contentColor: Color = MaterialTheme.colorScheme.onBackground.reversed,
    actionColor: Color = MaterialTheme.colorScheme.primary,
    actionContentColor: Color = MaterialTheme.colorScheme.primary,
    dismissActionContentColor: Color = SnackbarDefaults.dismissActionContentColor,
) {
    val actionLabel = snackbarData.visuals.actionLabel
    val actionComposable: (@Composable () -> Unit)? =
        if (actionLabel != null) {
            @Composable {
                TextButton(
                    colors = ButtonDefaults.textButtonColors(contentColor = actionColor),
                    onClick = { snackbarData.performAction() },
                    content = {
                        Text(text = actionLabel, style = MaterialTheme.typography.labelSmall)
                    }
                )
            }
        } else {
            null
        }
    val dismissActionComposable: (@Composable () -> Unit)? =
        if (snackbarData.visuals.withDismissAction) {
            @Composable {
                IconButton(
                    onClick = { snackbarData.dismiss() },
                    content = {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                        )
                    }
                )
            }
        } else {
            null
        }
    Snackbar(
        modifier = modifier.padding(12.dp),
        action = actionComposable,
        dismissAction = dismissActionComposable,
        actionOnNewLine = actionOnNewLine,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        actionContentColor = actionContentColor,
        dismissActionContentColor = dismissActionContentColor,
        content = {
            Text(
                text = snackbarData.visuals.message,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}