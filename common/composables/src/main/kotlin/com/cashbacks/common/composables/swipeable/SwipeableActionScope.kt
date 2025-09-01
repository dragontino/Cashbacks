package com.cashbacks.common.composables.swipeable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.cashbacks.common.utils.OnClick

@Composable
fun RowScope.ActionIcon(
    onClick: OnClick,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    contentDescription: String? = null,
    containerColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onBackground
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.background(containerColor),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = iconModifier,
            tint = contentColor
        )
    }
}


@Composable
fun RowScope.EditDeleteActions(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    editButtonColors: IconButtonColors = IconButtonDefaults.iconButtonColors(
        contentColor = MaterialTheme.colorScheme.primary
    ),
    deleteButtonColors: IconButtonColors = IconButtonDefaults.iconButtonColors(
        contentColor = MaterialTheme.colorScheme.primary
    )
) {
    ActionIcon(
        onClick = onEditClick,
        icon = Icons.Rounded.Edit,
        contentDescription = "edit",
        containerColor = editButtonColors.containerColor,
        contentColor = editButtonColors.contentColor,
        modifier = Modifier.fillMaxHeight(),
        iconModifier = Modifier.scale(1.1f)
    )

    ActionIcon(
        onClick = onDeleteClick,
        icon = Icons.Rounded.DeleteForever,
        contentDescription = "delete",
        containerColor = deleteButtonColors.containerColor,
        contentColor = deleteButtonColors.contentColor,
        modifier = Modifier.fillMaxHeight(),
        iconModifier = Modifier.scale(1.1f)
    )
}