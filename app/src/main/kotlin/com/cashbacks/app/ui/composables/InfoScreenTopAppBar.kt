package com.cashbacks.app.ui.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditOff
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import com.cashbacks.app.util.animate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InfoScreenTopAppBar(
    title: String,
    isInEdit: State<Boolean>,
    isLoading: State<Boolean>,
    onEdit: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    iconSave: ImageVector = Icons.Outlined.EditOff
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            AnimatedContent(
                targetState = isInEdit.value,
                contentAlignment = Alignment.Center,
                transitionSpec = {
                    val slideSpec = tween<IntOffset>(durationMillis = 300, easing = LinearEasing)
                    val scaleSpec = tween<Float>(durationMillis = 300, easing = LinearEasing)

                    val enter = when {
                        targetState -> slideInVertically(slideSpec)
                        else -> slideInHorizontally(slideSpec)
                    } + scaleIn(scaleSpec)
                    val exit = when {
                        targetState -> slideOutHorizontally(slideSpec)
                        else -> slideOutVertically(slideSpec)
                    } + scaleOut(scaleSpec)

                    return@AnimatedContent enter togetherWith exit
                },
                label = "navigationIconAnimation"
            ) { isEditing ->
                IconButton(
                    onClick = {
                        if (isEditing) onDelete()
                        onBack()
                    },
                    enabled = !isLoading.value
                ) {
                    Icon(
                        imageVector = when {
                            isEditing -> Icons.Rounded.DeleteOutline
                            else -> Icons.Rounded.ArrowBackIosNew
                        },
                        contentDescription = null,
                        modifier = Modifier.scale(1.2f)
                    )
                }
            }
        },
        actions = {
            Crossfade(
                targetState = isInEdit.value,
                animationSpec = tween(durationMillis = 300, easing = LinearEasing),
                label = "actionIconsAnimation"
            ) { isEditing ->
                IconButton(
                    onClick = {
                        if (isEditing) onSave() else onEdit()
                    },
                    enabled = !isLoading.value
                ) {
                    Icon(
                        imageVector = when {
                            isEditing -> iconSave
                            else -> Icons.Outlined.Edit
                        },
                        contentDescription = null,
                        modifier = Modifier.scale(1.2f)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary.animate(),
            titleContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary.animate()
        )
    )
}