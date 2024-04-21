package com.cashbacks.app.ui.composables

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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LiveData
import com.cashbacks.app.ui.theme.CashbacksTheme
import com.cashbacks.app.util.Loading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T : Any> ExposedDropdownMenuBoxScope.DropdownMenu(
    listLiveData: LiveData<out List<T>>,
    expanded: Boolean,
    onClose: () -> Unit,
    listContent: @Composable (ColumnScope.(List<T>) -> Unit)
) {
    val list = listLiveData.observeAsState().value

    ExposedDropdownMenu(
        expanded = expanded,
        onDismissRequest = onClose,
        modifier = Modifier.fillMaxWidth()
    ) {
        when (list) {
            null -> Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Loading()
            }
            else -> listContent(list)
        }
    }
}


@Suppress("UnusedReceiverParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T : Any> ColumnScope.DropdownMenuListContent(
    list: List<T>,
    selectedItem: T?,
    onClick: (T) -> Unit,
    title: (T) -> CharSequence = { it.toString() },
    leadingIcon: @Composable ((T) -> Unit)? = null,
    addButton: @Composable (() -> Unit)? = null,
) {
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
                if (item == selectedItem) {
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
                selectedItem = list[3],
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