package com.cashbacks.app.ui.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.cashbacks.app.util.animate
import com.cashbacks.domain.model.Cashback

@Composable
fun CashbackComposable(
    cashback: Cashback,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    ScrollableListItem(
        hiddenContent = {
            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary.animate()
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "delete",
                    modifier = Modifier.scale(1.2f)
                )
            }
        },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        ElevatedCard(
            onClick = onClick,
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 4.dp
            ),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface.animate()
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = """
                Кэшбек: значение = ${cashback.amount}%
                Карта: ${cashback.bankCard.hiddenLastDigitsOfNumber} 
                Комментарий: ${cashback.comment}
                """.trimIndent(),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}