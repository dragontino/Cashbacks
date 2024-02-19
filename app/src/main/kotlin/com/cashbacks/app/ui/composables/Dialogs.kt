package com.cashbacks.app.ui.composables

import androidx.compose.foundation.border
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashbacks.app.R
import com.cashbacks.app.util.animate

@Composable
fun ConfirmExitWithSaveDataDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onClose: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(
                shape = MaterialTheme.shapes.medium,
                onClick = {
                    onConfirm()
                    onClose()
                }
            ) {
                Text(stringResource(R.string.save), style = MaterialTheme.typography.bodyMedium)
            }
        },
        dismissButton = {
            TextButton(
                shape = MaterialTheme.shapes.medium,
                onClick = {
                    onDismiss()
                    onClose()
                }
            ) {
                Text(stringResource(R.string.do_not_save), style = MaterialTheme.typography.bodyMedium)
            }
        },
        text = {
            Text(
                text = stringResource(R.string.unsaved_data),
                style = MaterialTheme.typography.titleMedium.copy(
                    textAlign = TextAlign.Center,
                    lineHeight = 35.sp
                )
            )
        },
        shape = MaterialTheme.shapes.medium,
        containerColor = MaterialTheme.colorScheme.surface.animate(),
        textContentColor = MaterialTheme.colorScheme.onBackground.animate(),
        titleContentColor = MaterialTheme.colorScheme.onBackground.animate(),
        modifier = Modifier.border(
            width = 1.5.dp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            shape = MaterialTheme.shapes.medium
        )
    )
}


@Composable
fun ConfirmDeletionDialog(text: String, onConfirm: () -> Unit, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onClose()
                },
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    text = buildString {
                        append(stringResource(R.string.yes))
                        append(", ")
                        append(stringResource(R.string.delete).lowercase())
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onClose,
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    text = buildString {
                        append(stringResource(R.string.no))
                        append(", ")
                        append(stringResource(R.string.save).lowercase())
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.confirm_deletion),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(lineHeight = 35.sp),
            )
        },
        shape = MaterialTheme.shapes.medium,
        containerColor = MaterialTheme.colorScheme.surface.animate(),
        titleContentColor = MaterialTheme.colorScheme.onBackground.animate(),
        textContentColor = MaterialTheme.colorScheme.onBackground.animate(),
        modifier = Modifier.border(
            width = 1.5.dp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            shape = MaterialTheme.shapes.medium
        )
    )
}