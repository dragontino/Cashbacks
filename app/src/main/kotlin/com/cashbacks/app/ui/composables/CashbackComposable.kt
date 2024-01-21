package com.cashbacks.app.ui.composables

import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.cashbacks.app.util.animate
import com.cashbacks.domain.model.Cashback

@Composable
fun CashbackComposable(
    cashback: Cashback,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ScrollableListItem(
        hiddenContent = {
            EditDeleteContent(
                onEditClick = onEdit,
                onDeleteClick = onDelete,
            )
        }
    ) {
        ElevatedCard(
            onClick = onClick,
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 4.dp
            ),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface.animate()
            )
        ) {
            Text(
                text = """
                Кэшбек: значение = ${cashback.amount}%
                Карта: ${cashback.bankCard.hiddenNumber} 
                Комментарий: ${cashback.comment}
                """.trimIndent(),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}