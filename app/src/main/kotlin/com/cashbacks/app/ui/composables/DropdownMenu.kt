package com.cashbacks.app.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cashbacks.app.ui.managment.ListState
import com.cashbacks.app.ui.theme.CashbacksTheme
import com.cashbacks.app.util.Loading
import com.cashbacks.app.util.animate
import kotlinx.coroutines.flow.StateFlow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T : Any> ExposedDropdownMenuBoxScope.DropdownMenu(
    itemsFlow: StateFlow<List<T>?>,
    expanded: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (ColumnScope.(List<T>) -> Unit)
) {
    val listState = ListState.fromList(itemsFlow.collectAsStateWithLifecycle().value)

    ExposedDropdownMenu(
        expanded = expanded,
        onDismissRequest = onClose,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.animate()
        ),
        modifier = modifier
    ) {
        when (listState) {
            is ListState.Loading -> Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Loading()
            }

            is ListState.Empty -> content(emptyList())

            is ListState.Stable -> content(listState.data)
        }
    }
}


@Suppress("UnusedReceiverParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T : Any> ColumnScope.DropdownMenuListContent(
    list: List<T>,
    selected: (T) -> Boolean,
    onClick: (T) -> Unit,
    title: (T) -> CharSequence = { it.toString() },
    leadingIcon: @Composable ((T) -> Unit)? = null,
    addButton: @Composable (() -> Unit)? = null,
) {
    @Composable
    fun getBackgroundColorForPosition(position: Int) = when (position % 2) {
        0 -> MaterialTheme.colorScheme.background
        else -> MaterialTheme.colorScheme.surface
    }

    list.forEachIndexed { index, item ->
        DropdownMenuItem(
            text = {
                when (val text = title(item)) {
                    is String -> Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    is AnnotatedString -> Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

            },
            onClick = { onClick(item) },
            leadingIcon = { leadingIcon?.invoke(item) },
            trailingIcon = {
                if (selected(item)) {
                    Icon(imageVector = Icons.Rounded.Check, contentDescription = null)
                }
            },
            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
        )
        if (index < list.lastIndex) {
            HorizontalDivider()
        }
    }

    if (addButton != null) {
        HorizontalDivider()
        addButton()
    }
}


@Preview
@Composable
private fun DropdownMenuListContentPreview() {
    CashbacksTheme(isDarkTheme = false) {
        Column(Modifier.background(MaterialTheme.colorScheme.background)) {
            val list = List(8) { "Item ${it + 1}" }
            DropdownMenuListContent(
                list = list,
                selected = { it == list[3] },
                onClick = {},
                addButton = {
                    Text(
                        text = "Add item",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
        }
    }
}